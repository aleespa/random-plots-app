import os
import importlib
from matplotlib.colors import ListedColormap
import matplotlib.colors as mcolors

script_dir = os.path.dirname(os.path.realpath(__file__))

def generate(seed, dark_mode, color, script, colormap_colors):
    selected_script = importlib.import_module(f"figures.{script}")
    print(colormap_colors)
    cmap = create_colormap(colormap_colors)
    image = selected_script.create_image(seed, dark_mode, color, cmap)
    return image

def create_colormap(color_list):
    # Convert hex list like ["#FF0000", "#00FF00", "#0000FF"] into normalized stops
    n = len(color_list)
    stops = [(i / (n - 1), c) for i, c in enumerate(color_list)]
    return mcolors.LinearSegmentedColormap.from_list("custom_map", stops)