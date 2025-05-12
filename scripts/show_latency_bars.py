#!/usr/bin/env python3

"""
Latency Ascii Graph Visualizer

This script reads from stdin a structured latency dump and visualizes per-server and per-disk latency trends as ASCII-style time-series bar charts.

CLI Arguments:
--trim <fraction>   : Removes lowest and highest fraction of values (e.g. 0.01 = 1%)
--sort              : Sort before trimming (used for percentile-based trimming)
--width <columns>   : Sets fixed width of each disk graph
--debug             : Enable debug output to stderr

"""

import sys
import re
import shutil
import json
import argparse
from collections import defaultdict
import statistics
import math

def parse_input():
	data = defaultdict(lambda: defaultdict(list))
	try:
		parsed = json.load(sys.stdin)
		for node in parsed:
			ip = node.get("address", {}).get("ip")
			port = node.get("address", {}).get("port")
			server_label = f"/{ip}:{port}"
			for disk in node.get("disks", []):
				location = disk.get("location", "/unknown")
				label = location.rstrip("/").split("/")[-1]
				full_label = f"  {label}"
				for chunk in disk.get("chunks", []):
					latency_str = chunk.get("attachment", {}).get("latency")
					if latency_str and latency_str.startswith("PT") and latency_str.endswith("S"):
						try:
							value = float(latency_str[2:-1])
							data[server_label][full_label].append(value)
						except ValueError:
							continue
	except Exception as e:
		print(f"Error: {e}", file=sys.stderr)
		sys.exit(1)
	return data

def trim_outliers(latencies, fraction, sort=False):
	if not latencies or fraction <= 0.0 or fraction >= 0.5:
		return latencies
	if sort:
		latencies_sorted = sorted(latencies)
		n = len(latencies_sorted)
		low = int(n * fraction)
		high = n - low
		return latencies_sorted[low:high]
	else:
		q_low = statistics.quantiles(latencies, n=100)[int(fraction * 100)]
		q_high = statistics.quantiles(latencies, n=100)[int((1 - fraction) * 100) - 1]
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
		if end > len(latencies):
			end = len(latencies)
		chunk = latencies[start:end]
		if not chunk:
			chunk = [latencies[-1]]
		result.append(sum(chunk) / len(chunk))
	return result[:width]

def render_double_line_vertical_bar(latencies, max_value, width=50, debug=False):
	chars = " ▁▂▃▄▅▆▇█"
	steps = len(chars) - 1
	if not latencies:
		return ["(no data)", ""]
	latencies = downsample_mean(latencies, width)
	if debug:
		print(f"[DEBUG] Downsampled {len(latencies)} points for width={width}: {latencies}", file=sys.stderr)
	top_row = []
	bottom_row = []
	for val in latencies:
		norm = val / max_value if max_value else 0
		level = int(norm * steps * 2)
		if level >= steps * 2:
			bottom_row.append(chars[steps])
			top_row.append(chars[steps])
		elif level >= steps:
			bottom_row.append(chars[steps])
			top_row.append(chars[level - steps])
		elif level > 0:
			bottom_row.append(chars[level])
			top_row.append(" ")
		else:
			bottom_row.append(" ")
			top_row.append(" ")
	return ["".join(top_row), "".join(bottom_row)]

def display(data, trim_fraction, sort_outliers, graph_width, debug):
	print("\nLatency Time-Series Graphs (2-line stacked gradient, width={}, trimmed {}%, sort={}, debug={})".format(
		graph_width, int(trim_fraction * 100), sort_outliers, debug
	))
	print("-" * 70)
	all_latencies = []
	for server in data.values():
		for drive in server.values():
			all_latencies.extend(drive)
	if debug:
		print(f"[DEBUG] Total raw latencies: {len(all_latencies)}", file=sys.stderr)
	trimmed_all = trim_outliers(all_latencies, trim_fraction, sort_outliers)
	if debug:
		print(f"[DEBUG] Total after trim {trim_fraction}: {len(trimmed_all)}", file=sys.stderr)
	max_latency = max(trimmed_all) if trimmed_all else 1.0

	trimmed_set = set(trimmed_all)
	for server in sorted(data):
		print(f"\n{server}")
		for drive in sorted(data[server]):
			latencies = [v for v in data[server][drive] if v in trimmed_set]
			if debug:
				print(f"[DEBUG] {drive} -> {len(latencies)} latencies after global trim", file=sys.stderr)
			top, bottom = render_double_line_vertical_bar(latencies, max_latency, graph_width, debug)
			label = f"{drive:<28.28}"
			print(f"{label}│{top}")
			print(f"{' ' * 28}│{bottom}")

def parse_args():
	parser = argparse.ArgumentParser(description="Latency Ascii Graph Visualizer")
	parser.add_argument("--trim", type=float, default=0.0, help="Fraction to trim from both ends (0.0 - 0.5)")
	parser.add_argument("--sort", action="store_true", help="Sort before trimming")
	parser.add_argument("--width", type=int, default=None, help="Width of the output graph")
	parser.add_argument("--debug", action="store_true", help="Enable debug output")
	return parser.parse_args()

if __name__ == '__main__':
	args = parse_args()
	if args.width is None:
		try:
			total_columns = shutil.get_terminal_size((100, 20)).columns
			args.width = max(1, total_columns - 30)
		except:
			args.width = 50
	data = parse_input()
	display(data, args.trim, args.sort, args.width, args.debug)

