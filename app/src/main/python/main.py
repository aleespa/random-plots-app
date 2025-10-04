import os
import importlib
from matplotlib.colors import ListedColormap
import matplotlib.colors as mcolors

script_dir = os.path.dirname(os.path.realpath(__file__))

def generate(seed, color, script, colormap_colors):
    selected_script = importlib.import_module(f"figures.{script}")
    cmap = create_colormap(colormap_colors)
    image = selected_script.create_image(seed, color, cmap)
    return image

def create_colormap(color_list):
    n = len(color_list)
    stops = [(i / (n - 1), c) for i, c in enumerate(color_list)]
    return mcolors.LinearSegmentedColormap.from_list("custom_map", stops)