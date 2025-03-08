import numpy as np

import matplotlib as mpl


class ColorSelector:
    def __init__(self, colormap, background=False):
        self.colormap = colormap
        self.background = background
        self.brightness_threshold = 0.6 if background == 'light' else 0.4
        self.suitable_colors = self._precompute_colors()

    def _precompute_colors(self, n_samples=1000):
        """Precompute suitable colors"""
        samples = np.linspace(0, 1, n_samples)
        colors = [self.colormap(x) for x in samples]
        return [c for c in colors if self._is_suitable(c)]

    def _is_suitable(self, color):
        brightness = perceived_brightness(color)
        if not self.background:
            return brightness < self.brightness_threshold
        return brightness > self.brightness_threshold

    def get_color(self, rng):
        """Get random suitable color"""
        return rng.choice(self.suitable_colors)


def perceived_brightness(color):
    """Calculate perceived brightness of a color (0-1 scale)"""
    # Convert to RGB if color is in other format
    if isinstance(color, str):  # Hex color
        color = mpl.colors.to_rgb(color)
    elif len(color) == 4:  # RGBA
        color = color[:3]

    # Weighted sum of RGB channels
    r, g, b = color
    return 0.2126 * r + 0.7152 * g + 0.0722 * b
