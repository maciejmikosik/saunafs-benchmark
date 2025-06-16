#!/usr/bin/env python3
import json
import sys
from datetime import datetime

def parse_time_ns(iso):
	ts, ns = iso.rstrip("Z").split(".")
	dt = datetime.strptime(ts, "%Y-%m-%dT%H:%M:%S")
	return int(dt.timestamp() * 1e9) + int(ns[:9].ljust(9, '0'))

def calculate_speed(size_bytes, start, end):
	duration_sec = (end - start) / 1e9
	if duration_sec <= 0:
		return 0.0, 0.0
	speed_mib = (size_bytes / 1024 / 1024) / duration_sec
	return duration_sec, speed_mib

def svg_chart(speeds):
	if not speeds:
		return ""
	width = 400
	height = 100
	bar_width = width // len(speeds)
	max_speed = max(speeds)
	avg_speed = sum(speeds) / len(speeds)

	svg = [
		f'<svg width="{width}" height="{height}" viewBox="0 0 {width} {height}" xmlns="http://www.w3.org/2000/svg">'
	]

	for i, spd in enumerate(speeds):
		h = int((spd / max_speed) * (height - 20))
		svg.append(
			f'<rect x="{i * bar_width}" y="{height - h}" width="{bar_width - 1}" height="{h}" fill="#60a5fa" />'
		)

	y_avg = height - int((avg_speed / max_speed) * (height - 20))
	svg.append(f'<line x1="0" y1="{y_avg}" x2="{width}" y2="{y_avg}" stroke="red" stroke-width="1" />')
	svg.append('</svg>')
	return "".join(svg)

def svg_server_chart(disk_speeds):
	if not disk_speeds:
		return ""
	width = 600
	height = 150
	bar_width = width // len(disk_speeds)
	max_speed = max(disk_speeds.values())
	avg_speed = sum(disk_speeds.values()) / len(disk_speeds)

	svg = [
		f'<svg width="{width}" height="{height}" viewBox="0 0 {width} {height}" xmlns="http://www.w3.org/2000/svg">'
	]
	for i, (disk, spd) in enumerate(disk_speeds.items()):
		h = int((spd / max_speed) * (height - 40))
		x = i * bar_width
		svg.append(f'<rect x="{x}" y="{height - h - 20}" width="{bar_width - 4}" height="{h}" fill="#93c5fd" />')
		svg.append(f'<text x="{x + 2}" y="{height - 4}" font-size="10" transform="rotate(45 {x + 2},{height - 4})">{disk}</text>')

	y_avg = height - 20 - int((avg_speed / max_speed) * (height - 40))
	svg.append(f'<line x1="0" y1="{y_avg}" x2="{width}" y2="{y_avg}" stroke="darkred" stroke-width="1" stroke-dasharray="4" />')
	svg.append('</svg>')
	return "".join(svg)

def main():
	data = json.load(sys.stdin)
	print("""<!DOCTYPE html>
<html lang=\"en\">
<head>
	<meta charset=\"UTF-8\">
	<title>Benchmark Report</title>
	<style>
		body { font-family: sans-serif; margin: 20px; background: #f8fafc; }
		.server { margin-bottom: 40px; border: 1px solid #ccc; padding: 10px; border-radius: 8px; background: #fff; display: flex; justify-content: space-between; }
		.server-content { width: 65%; }
		.server-chart { width: 30%; }
		.disk-header { cursor: pointer; font-weight: bold; margin: 8px 0; background: #e0f2fe; padding: 8px; border-radius: 4px; }
		.chunk-details { display: none; margin-left: 20px; }
		.chunk { display: flex; gap: 10px; padding: 4px 0; }
		.svg-chart { margin-top: 8px; }
	</style>
	<script>
		function toggle(e) {
			let n = e.nextElementSibling;
			n.style.display = n.style.display === 'block' ? 'none' : 'block';
		}
	</script>
</head>
<body>""")
	print(f"<h1>Benchmark: {data['benchmark'].capitalize()}</h1>")

	for node in data['cluster']:
		ip = node['address']['ip']
		port = node['address']['port']
		print(f'<div class="server"><div class="server-content"><h2>{ip}:{port}</h2>')
		disk_speeds = {}
		for disk in node['disks']:
			path = disk['location'] or '(unknown)'
			speeds = []
			print(f'<div class="disk-header" onclick="toggle(this)">{path}</div><div class="chunk-details">')
			for chunk in disk.get('chunks', []):
				cid = chunk['id']
				size = chunk['size']
				begin = parse_time_ns(chunk['result']['time']['begin'])
				end = parse_time_ns(chunk['result']['time']['end'])
				duration, speed = calculate_speed(size, begin, end)
				speeds.append(speed)
				print(f'<div class="chunk"><span>ID: {cid}</span><span>{duration:.6f}s</span><span>{size} B</span><span>{speed:.3f} MiB/s</span></div>')
			chart = svg_chart(speeds)
			avg = sum(speeds)/len(speeds) if speeds else 0
			disk_speeds[path.split('/')[-2]] = avg
			print(f'<div class="svg-chart">{chart}</div></div>')
		server_chart = svg_server_chart(disk_speeds)
		print(f'</div><div class="server-chart">{server_chart}</div></div>')
	print("</body></html>")

if __name__ == "__main__":
	main()

