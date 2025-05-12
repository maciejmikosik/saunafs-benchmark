#!/usr/bin/env python3

"""
Latency Ascii Graph Visualizer

This script reads from stdin a structured latency dump containing per-server and per-disk latency
measurements in the following format:

- Lines starting with a single slash (e.g. /192.168.50.201:9422) indicate a new server section.
- Lines starting with two spaces and a slash (e.g.   /mnt/data/ata-XYZ/) indicate a new disk under that server.
- Following each disk line are multiple indented entries of the form:

      <sequence_number>    <latency>s

Assumptions:
- Latency values are in seconds and always end with the 's' suffix.
- Disk identifiers can be extracted by splitting the path and taking the penultimate component.
- A single server may contain multiple drives, each with a sequence of latency values.

Graphing logic:
- For each disk, a time-series-style bar graph is drawn across a fixed-width horizontal line.
- To improve resolution, each disk graph uses two stacked lines:
    - Bottom line draws the primary bar using a gradient block character.
    - Top line optionally draws overflow or high-resolution top caps.
- Characters used: " ▁▂▃▄▅▆▇█" (increasing density left to right)

Command-line arguments:
--trim <fraction>   : Removes lowest and highest fraction of values (e.g. 0.01 for 1%)
--sort              : Sorts values before trimming (used for percentile display)
--width <columns>   : Sets fixed width of the graph. Defaults to terminal width - 26

To support future format changes:
- Consider abstracting input format parsing into a strategy class or configurable schema.
- Extend character rendering to support Unicode Braille for denser graphs.
- Add support for outputting statistics or alternate layouts (e.g. per-host aggregates).
"""
import sys
import re
import shutil
from collections import defaultdict
import statistics
import math

def parse_input():
	data = defaultdict(lambda: defaultdict(list))
	current_server = None
	current_drive = None
	for line in sys.stdin:
		if line.startswith('/'):
			current_server = line.strip()
		elif line.startswith('  /'):
			current_drive = line.strip()
		elif current_server and current_drive:
			match = re.search(r'([\d.]+)s', line)
			if match:
				latency = float(match.group(1))
				data[current_server][current_drive].append(latency)
	return data

def trim_outliers(latencies, fraction, sort=False):
	if not latencies or fraction == 0.0:
		return latencies
	if sort:
		latencies_sorted = sorted(latencies)
		n = len(latencies_sorted)
		low = int(n * fraction)
		high = n - low
		return latencies_sorted[low:high]
	else:
		q_low = statistics.quantiles(latencies, n=100)[int(fraction*100)]
		q_high = statistics.quantiles(latencies, n=100)[int((1-fraction)*100)-1]
		return [x for x in latencies if q_low <= x <= q_high]

def downsample_mean(latencies, width):
	if width < 1:
		return []
	if len(latencies) <= width:
		return latencies + [latencies[-1]] * (width - len(latencies))
	step = len(latencies) / width
	result = []
	for i in range(width):
		start = int(i * step)
		end = int((i + 1) * step)
		chunk = latencies[start:end] if end > start else latencies[start:start+1]
		result.append(sum(chunk) / len(chunk))
	return result

def render_double_line_vertical_bar(latencies, max_value, width=50):
	chars = " ▁▂▃▄▅▆▇█"
	steps = len(chars) - 1
	if not latencies:
		return ["(no data)", ""]

	latencies = downsample_mean(latencies, width)

	top_row = ""
	bottom_row = ""
	for val in latencies:
		norm = val / max_value if max_value else 0
		level = int(norm * steps * 2)
		if level >= steps * 2:
			bottom_row += chars[steps]
			top_row += chars[steps]
		elif level >= steps:
			bottom_row += chars[steps]
			top_row += chars[level - steps]
		elif level > 0:
			bottom_row += chars[level]
			top_row += " "
		else:
			bottom_row += " "
			top_row += " "

	return [top_row, bottom_row]

def display(data, trim_fraction, sort_outliers, graph_width):
	print(f"\nLatency Time-Series Graphs (2-line stacked gradient, trimmed {int(trim_fraction * 100)}%, sort={sort_outliers}), width={graph_width} \n" + "-"*70)
	all_latencies = [lat for server in data.values() for drive in server.values() for lat in trim_outliers(drive, trim_fraction, sort_outliers)]
	max_latency = max(all_latencies) if all_latencies else 1.0

	for server in sorted(data):
		print(f"\n{server}")
		for drive in sorted(data[server]):
			latencies = trim_outliers(data[server][drive], trim_fraction, sort_outliers)
			top, bottom = render_double_line_vertical_bar(latencies, max_latency, graph_width)
			label = f"  {drive.split('/')[-2]:<24}"
			print(f"{label} {top}")
			print(f"{' '*len(label)} {bottom}")

if __name__ == '__main__':
	trim_fraction = 0.0
	sort_outliers = False
	graph_width = None

	for i, arg in enumerate(sys.argv):
		if arg == '--trim' and i + 1 < len(sys.argv):
			try:
				trim_fraction = float(sys.argv[i + 1])
			except ValueError:
				print("Invalid value for --trim. Using default 0.0.")
		elif arg == '--sort':
			sort_outliers = True
		elif arg == '--width' and i + 1 < len(sys.argv):
			try:
				graph_width = int(sys.argv[i + 1])
			except ValueError:
				print("Invalid value for --width. Will use terminal size.")

	if graph_width is None:
		try:
			terminal_width = shutil.get_terminal_size((100, 20)).columns
			graph_width = max(10, terminal_width - 26)
		except:
			graph_width = 50

	data = parse_input()
	display(data, trim_fraction, sort_outliers, graph_width)

