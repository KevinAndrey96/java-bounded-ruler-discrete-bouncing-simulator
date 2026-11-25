#!/usr/bin/env python3
"""
Bounded Ruler Discrete Bouncing Simulator
Verification suite for architecture, closed-form mathematics, business rules, CLI, benchmarks,
containerization and the zero em-dash quality rule.

Only the Python standard library is required.
"""

import csv
import os
import random
import re
import shutil
import subprocess
import sys

ROOT_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
PACKAGE_DIR = "src/main/java/com/kevinandrey/simulation/bouncing"
TEST_DIR = "src/test/java/com/kevinandrey/simulation/bouncing"
CLASSES_DIR = os.path.join(ROOT_DIR, "target", "verify-classes")
MAIN_CLASS = "com.kevinandrey.simulation.bouncing.cli.BouncingCli"
SCENARIOS = os.path.join("scenarios", "reference-scenarios.csv")
EM_DASH = "\u2014"


def log_test(name, passed, details=""):
    status = "PASS" if passed else "FAIL"
    print(f"[{status}] {name}")
    if details:
        print(f"       {details}")
    return passed


# Independent Python reference model


def brute_force(length, start, direction, steps):
    position = start
    for _ in range(steps):
        if direction == 1 and position == length:
            direction = -1
        elif direction == -1 and position == 0:
            direction = 1
        position += direction
    return position, direction


def periodic_reduction(length, start, direction, steps):
    if steps == 0:
        return start, direction
    return brute_force(length, start, direction, (steps - 1) % (2 * length) + 1)


def triangular_wave(phase, length):
    return length - abs(length - phase)


def closed_form(length, start, direction, steps):
    if steps == 0:
        return start, direction
    period = 2 * length
    offset = start if direction == 1 else (period - start) % period
    phase = (offset + steps % period) % period
    final_direction = 1 if 1 <= phase <= length else -1
    return triangular_wave(phase, length), final_direction


def run_cli(*args, timeout=180):
    command = ["java", "-Djava.awt.headless=true", "-cp", CLASSES_DIR, MAIN_CLASS] + list(args)
    return subprocess.run(command, capture_output=True, text=True, cwd=ROOT_DIR, timeout=timeout)


def parse_final_state(output):
    position = re.search(r"Final Position\s*:\s*(-?\d+)", output)
    direction = re.search(r"Final Direction\s*:\s*([+-]1)", output)
    if not position or not direction:
        return None
    return int(position.group(1)), int(direction.group(1))


# Suite 1


def test_architecture_and_files():
    name = "Suite 1: Clean Architecture & Maven Project Completeness"
    required = [
        "pom.xml",
        "Dockerfile",
        "docker-compose.yml",
        ".dockerignore",
        ".gitignore",
        ".github/workflows/ci.yml",
        "README.md",
        SCENARIOS,
        f"{PACKAGE_DIR}/RulerBouncingSimulator.java",
        f"{PACKAGE_DIR}/domain/Direction.java",
        f"{PACKAGE_DIR}/domain/RulerConfiguration.java",
        f"{PACKAGE_DIR}/domain/RulerState.java",
        f"{PACKAGE_DIR}/domain/BounceRule.java",
        f"{PACKAGE_DIR}/domain/InvalidConfigurationException.java",
        f"{PACKAGE_DIR}/solver/BouncingSolver.java",
        f"{PACKAGE_DIR}/solver/StepByStepSolver.java",
        f"{PACKAGE_DIR}/solver/PeriodicReductionSolver.java",
        f"{PACKAGE_DIR}/solver/AnalyticalSolver.java",
        f"{PACKAGE_DIR}/solver/SolverCatalog.java",
        f"{PACKAGE_DIR}/trajectory/Trajectory.java",
        f"{PACKAGE_DIR}/trajectory/TrajectoryCsvExporter.java",
        f"{PACKAGE_DIR}/benchmark/SolverBenchmark.java",
        f"{PACKAGE_DIR}/scenario/ScenarioRunner.java",
        f"{PACKAGE_DIR}/cli/BouncingCli.java",
        f"{PACKAGE_DIR}/gui/RulerDashboardFrame.java",
        f"{PACKAGE_DIR}/gui/RulerRenderer.java",
        f"{TEST_DIR}/solver/SolverEquivalenceTest.java",
        f"{TEST_DIR}/solver/AnalyticalSolverTest.java",
        f"{TEST_DIR}/cli/BouncingCliTest.java",
    ]
    missing = [path for path in required if not os.path.exists(os.path.join(ROOT_DIR, path))]
    if missing:
        return log_test(name, False, f"Missing files: {missing}")

    forbidden = ["build", "nbproject", "build.xml", "manifest.mf", "src/juegoni\u00f1os", ".idea"]
    found = [path for path in forbidden if os.path.exists(os.path.join(ROOT_DIR, path))]
    if found:
        return log_test(name, False, f"Legacy NetBeans artifacts still present: {found}")

    binaries = []
    for root, dirs, files in os.walk(ROOT_DIR):
        dirs[:] = [d for d in dirs if d not in (".git", "target")]
        binaries.extend(os.path.join(root, f) for f in files if f.endswith((".class", ".jar", ".dll")))
    if binaries:
        return log_test(name, False, f"Compiled binaries committed: {binaries}")

    return log_test(name, True, f"All {len(required)} files present, zero binaries and zero NetBeans bloat.")


# Suite 2


def test_closed_form_mathematics():
    name = "Suite 2: Triangular Wave Closed-Form Formulation"
    checked = 0
    for length in range(1, 41):
        period = 2 * length
        for phase in range(period):
            folded = triangular_wave(phase, length)
            if not 0 <= folded <= length:
                return log_test(name, False, f"T({phase}) = {folded} outside [0, {length}]")
            if phase and folded != triangular_wave(period - phase, length):
                return log_test(name, False, f"Symmetry T(u) = T(2N - u) broken at N={length}, u={phase}")
        for start in range(length + 1):
            for direction in (1, -1):
                for steps in range(0, 4 * length + 3):
                    expected = brute_force(length, start, direction, steps)
                    if closed_form(length, start, direction, steps) != expected:
                        return log_test(name, False, f"Closed form mismatch N={length} P={start} d={direction} M={steps}")
                    if steps >= 1 and brute_force(length, start, direction, steps + period) != expected:
                        return log_test(name, False, f"Period 2N violated N={length} P={start} d={direction} M={steps}")
                    checked += 1
    return log_test(name, True, f"{checked} states prove T(u) = N - |N - u|, symmetry, range and exact period 2N.")


# Suite 3


def test_business_rules_and_edge_cases():
    name = "Suite 3: Reflection Rules, Seed Scenarios & Edge Cases"
    if closed_form(5, 4, 1, 4) != (2, -1):
        return log_test(name, False, "Original statement example N=5, P=4, d=1, M=4 must end at position 2")
    if closed_form(8, 8, 1, 0) != (8, 1):
        return log_test(name, False, "Zero steps must preserve the initial state even when facing a wall")
    if closed_form(5, 5, 1, 1) != (4, -1) or closed_form(5, 0, -1, 1) != (1, 1):
        return log_test(name, False, "Walls must reverse direction before the displacement")

    with open(os.path.join(ROOT_DIR, SCENARIOS), encoding="utf-8") as handle:
        rows = [line for line in handle if line.strip() and not line.startswith("#")]
    scenarios = list(csv.DictReader(rows))
    for row in scenarios:
        length, start = int(row["length"]), int(row["start"])
        direction, steps = int(row["direction"]), int(row["steps"])
        expected = (int(row["expected_position"]), int(row["expected_direction"]))
        reference = closed_form(length, start, direction, steps)
        if 2 * length <= 10_000_000:
            reference_periodic = periodic_reduction(length, start, direction, steps)
            if reference_periodic != reference:
                return log_test(name, False, f"Python models disagree on {row['name']}")
        if reference != expected:
            return log_test(name, False, f"Seed {row['name']} expects {expected}, model gives {reference}")
    return log_test(name, True, f"Statement example, wall reflections and {len(scenarios)} seed scenarios verified.")


# Suite 4


def compile_sources():
    if os.path.exists(CLASSES_DIR):
        shutil.rmtree(CLASSES_DIR)
    os.makedirs(CLASSES_DIR)
    sources = []
    for root, _, files in os.walk(os.path.join(ROOT_DIR, "src", "main", "java")):
        sources.extend(os.path.join(root, f) for f in files if f.endswith(".java"))
    return subprocess.run(["javac", "-encoding", "UTF-8", "-d", CLASSES_DIR] + sources,
                          capture_output=True, text=True)


def test_compilation_and_cli():
    name = "Suite 4: Java Compilation & Headless CLI Cross-Validation"
    compiled = compile_sources()
    if compiled.returncode != 0:
        return log_test(name, False, f"javac failed: {compiled.stderr}")

    default_run = run_cli()
    if default_run.returncode != 0 or parse_final_state(default_run.stdout) != (2, -1):
        return log_test(name, False, f"Default run must reproduce the statement example: {default_run.stdout}")

    rng = random.Random(1125)
    for _ in range(12):
        length = rng.randint(1, 10 ** 12)
        start = rng.randint(0, length)
        direction = rng.choice((1, -1))
        steps = rng.randint(0, 2 ** 62)
        result = run_cli("--length", str(length), "--start", str(start), "--direction", str(direction),
                         "--steps", str(steps))
        if result.returncode != 0:
            return log_test(name, False, f"CLI failed: {result.stderr}")
        if parse_final_state(result.stdout) != closed_form(length, start, direction, steps):
            return log_test(name, False, f"CLI disagrees with Python model for N={length} P={start} M={steps}")

    for solver in ("step", "periodic", "analytical"):
        result = run_cli("-n", "10", "-p", "3", "-d", "-1", "-m", "1000003", "--solver", solver)
        if parse_final_state(result.stdout) != (0, -1):
            return log_test(name, False, f"Solver {solver} returned {result.stdout}")

    invalid = run_cli("--length", "5", "--start", "9")
    if invalid.returncode != 2 or "start must be inside [0, 5]" not in invalid.stderr:
        return log_test(name, False, f"Invalid start must exit with code 2: {invalid.stderr}")

    seeds = run_cli("--scenarios", SCENARIOS)
    if seeds.returncode != 0 or "[FAIL]" in seeds.stdout:
        return log_test(name, False, f"Scenario mode failed: {seeds.stdout}")

    return log_test(name, True, "Compiled cleanly, 12 random huge cases, 3 solvers, validation and seeds agree.")


# Suite 5


def test_benchmark_and_complexity():
    name = "Suite 5: O(M) versus O(1) Benchmark & Convergence"
    result = run_cli("--length", "1000", "--start", "999", "--direction", "-1", "--steps", "200000000",
                     "--benchmark", "--iterations", "3")
    if result.returncode != 0 or "Consistency     : PASS" not in result.stdout:
        return log_test(name, False, f"Benchmark inconsistent: {result.stdout}{result.stderr}")

    timings = {}
    for line in result.stdout.splitlines():
        match = re.match(r"^(step|periodic|analytical)\s+\S+.*?\s(\d+)\s+([\d.]+)x$", line.strip())
        if match:
            timings[match.group(1)] = int(match.group(2))
    if set(timings) != {"step", "periodic", "analytical"}:
        return log_test(name, False, f"Could not parse benchmark table: {result.stdout}")
    if not timings["analytical"] < timings["periodic"] < timings["step"]:
        return log_test(name, False, f"Expected analytical < periodic < step, got {timings}")
    speedup = timings["step"] / timings["analytical"]
    if speedup < 1000:
        return log_test(name, False, f"Closed form speedup {speedup:.0f}x below 1000x")

    huge = run_cli("--steps", str(2 ** 63 - 1), "--benchmark", "--iterations", "1")
    if huge.returncode != 0 or "skipped" not in huge.stdout:
        return log_test(name, False, f"Astronomical M must skip the O(M) solver: {huge.stdout}")

    return log_test(name, True, f"M = 2e8: step {timings['step']} ns, analytical {timings['analytical']} ns "
                                f"({speedup:,.0f}x faster), O(M) skipped for M = 2^63 - 1.")


# Suite 6


def test_docker_and_ci_config():
    name = "Suite 6: Docker Containerization & CI/CD Pipeline"
    contents = {}
    for path in ("Dockerfile", "docker-compose.yml", ".github/workflows/ci.yml"):
        with open(os.path.join(ROOT_DIR, path), encoding="utf-8") as handle:
            contents[path] = handle.read()

    dockerfile = contents["Dockerfile"]
    if len(re.findall(r"^FROM ", dockerfile, re.MULTILINE)) < 2 or "AS builder" not in dockerfile:
        return log_test(name, False, "Dockerfile must be multi-stage")
    if not re.search(r"^USER\s+\S+", dockerfile, re.MULTILINE):
        return log_test(name, False, "Dockerfile must run as a non-root user")

    compose = contents["docker-compose.yml"]
    for token in ("services:", "simulator:", "benchmark:", "scenarios:", "reference-scenarios.csv"):
        if token not in compose:
            return log_test(name, False, f"docker-compose.yml missing {token}")

    ci = contents[".github/workflows/ci.yml"]
    for token in ("actions/checkout@v4", "actions/setup-java@v4", "verify_bouncing.py", "docker compose"):
        if token not in ci:
            return log_test(name, False, f"ci.yml missing {token}")
    for version in ("'8'", "'11'", "'17'", "'21'"):
        if version not in ci:
            return log_test(name, False, f"ci.yml matrix missing Java {version}")

    return log_test(name, True, "Multi-stage non-root Dockerfile, seeded Compose stack and JDK 8/11/17/21 matrix.")


# Suite 7


def test_zero_em_dash_compliance():
    name = "Suite 7: Zero Em-Dash Quality Rule (U+2014)"
    violations = []
    scanned = 0
    for root, dirs, files in os.walk(ROOT_DIR):
        dirs[:] = [d for d in dirs if d not in (".git", "target")]
        for file_name in files:
            path = os.path.join(root, file_name)
            try:
                with open(path, encoding="utf-8") as handle:
                    for line_number, line in enumerate(handle, 1):
                        if EM_DASH in line:
                            violations.append(f"{os.path.relpath(path, ROOT_DIR)}:{line_number}")
                scanned += 1
            except (UnicodeDecodeError, OSError):
                continue
    if violations:
        return log_test(name, False, f"Em-dash detected in: {violations}")
    return log_test(name, True, f"{scanned} text files scanned, zero U+2014 characters.")


def main():
    print("=" * 80)
    print("  BOUNDED RULER DISCRETE BOUNCING SIMULATOR")
    print("  Architecture, Closed-Form Mathematics & Benchmark Verification Suite")
    print("=" * 80)

    suites = [
        test_architecture_and_files,
        test_closed_form_mathematics,
        test_business_rules_and_edge_cases,
        test_compilation_and_cli,
        test_benchmark_and_complexity,
        test_docker_and_ci_config,
        test_zero_em_dash_compliance,
    ]
    results = []
    for suite in suites:
        try:
            results.append(suite())
        except Exception as error:
            results.append(log_test(suite.__name__, False, f"unexpected error: {error}"))

    shutil.rmtree(CLASSES_DIR, ignore_errors=True)
    passed = sum(1 for result in results if result)
    print("=" * 80)
    print(f"  Summary: {passed}/{len(results)} verification suites passed.")
    print("=" * 80)
    sys.exit(0 if passed == len(results) else 1)


if __name__ == "__main__":
    main()
