import os
import importlib
import threading
from matplotlib.colors import ListedColormap
import matplotlib.colors as mcolors
import matplotlib.pyplot as plt

script_dir = os.path.dirname(os.path.realpath(__file__))

_script_cache = {}
_cmap_cache = {}
_generate_lock = threading.Lock()

def generate(seed, color, script, colormap_colors):
    with _generate_lock:
        if script not in _script_cache:
            _script_cache[script] = importlib.import_module(f"figures.{script}")
            
        selected_script = _script_cache[script]
        
        color_tuple = tuple(colormap_colors)
        if color_tuple not in _cmap_cache:
            _cmap_cache[color_tuple] = create_colormap(color_tuple)
            
        cmap = _cmap_cache[color_tuple]
        
        image = selected_script.create_image(seed, color, cmap)
        
        # Ensure that no matplotlib memory or pyplot state is leaked
        plt.close('all')
        
        return image

def create_colormap(color_list):
    n = len(color_list)
    stops = [(i / (n - 1), c) for i, c in enumerate(color_list)]
    return mcolors.LinearSegmentedColormap.from_list("custom_map", stops)