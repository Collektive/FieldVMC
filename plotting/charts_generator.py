import subprocess
import sys
from pathlib import Path

PLOTTERS = (
    ("self-division", "plot-self-division.py", ()),
    ("self-integration", "plot-self-integration.py", ()),
    ("generic", "plot.py", ()),
    ("message size for leader-election", "plot-message-size.py", ("--experiment", "leader-election")),
    ("message size for fixed-leader", "plot-message-size.py", ("--experiment", "fixed-leader")),
)


def main():
    """Launch all chart generators from the plotting directory."""
    script_directory = Path(__file__).resolve().parent
    project_directory = script_directory.parent
    (project_directory / "charts").mkdir(exist_ok=True)

    for description, script_name, arguments in PLOTTERS:
        subprocess.Popen(
            [sys.executable, script_directory / script_name, *arguments],
            cwd=project_directory,
        )
        print(f"Launched {description} plotter script.")

    print("All chart generator scripts have been launched.")


if __name__ == "__main__":
    main()
