"""Query OSV for Gradle's resolved Maven release runtime graph."""
import argparse
from datetime import datetime, timezone
import json
from pathlib import Path
import re
import urllib.request


def resolved_packages(tree):
    edge = re.compile(r"(?:\+---|\\---) ([\w.-]+:[\w.-]+)(?::(\{[^}]+\}|[^ \n]+))?(?: -> ([^ \n]+))?")
    packages = set()
    for line in tree.splitlines():
        match = edge.search(line)
        if not match:
            continue
        coordinate, requested, selection = match.groups()
        version = selection or requested
        if version and version.count(":") == 2:
            coordinate, version = version.rsplit(":", 1)
        if version and not version.startswith("{") and ":" not in version:
            packages.add((coordinate, version))
    return sorted(packages)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("runtime_tree", type=Path)
    parser.add_argument("report", type=Path)
    args = parser.parse_args()
    packages = resolved_packages(args.runtime_tree.read_text())
    if not packages:
        raise SystemExit("No resolved Maven release dependencies found")
    queries = [dict(package=dict(ecosystem="Maven", name=name), version=version)
               for name, version in packages]
    request = urllib.request.Request(
        "https://api.osv.dev/v1/querybatch", data=json.dumps(dict(queries=queries)).encode(),
        headers={"Content-Type": "application/json"}, method="POST",
    )
    report = dict(source="https://api.osv.dev/v1/querybatch",
                  assessedAt=datetime.now(timezone.utc).isoformat(), coordinates=len(packages))
    try:
        with urllib.request.urlopen(request, timeout=30) as stream:
            results = json.load(stream)["results"]
        if len(results) != len(packages) or any(result.get("next_page_token") for result in results):
            raise ValueError("OSV returned a partial response")
        findings = [dict(package=name, version=version, advisories=[item["id"] for item in result["vulns"]])
                    for (name, version), result in zip(packages, results) if result.get("vulns")]
        report.update(status="REVIEW_ADVISORIES" if findings else "NO_OSV_MATCHES", findings=findings)
    except (OSError, ValueError, KeyError) as error:
        report.update(status="SCAN_UNAVAILABLE", detail=str(error))
    args.report.write_text(json.dumps(report, indent=2) + "\n")
    print(json.dumps(report, indent=2))


if __name__ == "__main__":
    main()
