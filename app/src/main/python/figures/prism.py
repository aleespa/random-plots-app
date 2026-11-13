"""Prism: an extended Random Art composition.

Builds on Andrej Bauer's Random Art (the public randomart.py that the app's Super/Soft/Noise/B&W
compositions follow) with the features his full generator at random-art.org appears to have,
judging by the operator names in its published sample formula:

  1. Palette colouring: the expression yields ONE value per pixel, mapped through a colormap
     (instead of three independent RGB channels, which look psychedelic).
  2. Focal points: random points shared by the whole image; leaves measure distance to a point,
     distance to the nearest point, cell edges between points, and waves around a point.
  3. Coordinate transforms: a subtree is evaluated at rotated, mirrored, n-fold kaleidoscope,
     tiled (torus) or polar coordinates. This is where symmetry and repetition come from.
  4. Hard edges: discretize, compare/select, min/max, so shapes can have crisp boundaries.
  5. Shared subexpressions: a finished node can be reused elsewhere in the formula (a DAG, not a
     tree). Reused under a different transform, it repeats the same motif across the image.

Every node evaluates to an array in [-1, 1] over the pixel grid.
"""
import io
import math

import matplotlib.pyplot as plt
import numpy as np

RESOLUTION = 800


# --------------------------------------------------------------------------------------------
# Evaluation context
# --------------------------------------------------------------------------------------------

class Scene:
    """Data shared by the whole formula: the focal points (the 'layout')."""

    def __init__(self, rng):
        n = int(rng.integers(2, 6))
        self.foci = rng.uniform(-0.8, 0.8, size=(n, 2))


def _clip(a):
    return np.clip(np.nan_to_num(a, nan=0.0, posinf=1.0, neginf=-1.0), -1.0, 1.0)


def _well(v):
    return 1 - 2 / (1 + v * v) ** 8


def _tent(v):
    return 1 - 2 * np.abs(v)


class Node:
    arity = 0
    children = ()

    def eval(self, scene, X, Y):
        raise NotImplementedError


# --- Leaves (coordinates, constants, focal-point fields) ------------------------------------

class VarX(Node):
    def __init__(self, rng, scene): pass

    def eval(self, scene, X, Y): return X

    def __repr__(self): return "x"


class VarY(Node):
    def __init__(self, rng, scene): pass

    def eval(self, scene, X, Y): return Y

    def __repr__(self): return "y"


class Const(Node):
    def __init__(self, rng, scene): self.c = rng.uniform(-1, 1)

    def eval(self, scene, X, Y): return np.full_like(X, self.c)

    def __repr__(self): return f"{self.c:.2f}"


class FocusDist(Node):
    """Distance to one focal point: concentric rings / halos."""

    def __init__(self, rng, scene):
        self.i = int(rng.integers(len(scene.foci)))
        self.scale = rng.uniform(0.8, 2.5)

    def eval(self, scene, X, Y):
        fx, fy = scene.foci[self.i]
        return _clip(1 - self.scale * np.hypot(X - fx, Y - fy))

    def __repr__(self): return f"dist(f{self.i})"


class NearestFocus(Node):
    """Distance to the closest focal point: rounded Voronoi cells."""

    def __init__(self, rng, scene): self.scale = rng.uniform(1.5, 4.0)

    def eval(self, scene, X, Y):
        d = np.min([np.hypot(X - fx, Y - fy) for fx, fy in scene.foci], axis=0)
        return _clip(1 - self.scale * d)

    def __repr__(self): return "nearest"


class CellEdges(Node):
    """Second-nearest minus nearest distance: bright seams between Voronoi cells."""

    def __init__(self, rng, scene): self.scale = rng.uniform(3.0, 8.0)

    def eval(self, scene, X, Y):
        d = np.sort([np.hypot(X - fx, Y - fy) for fx, fy in scene.foci], axis=0)
        return _clip(1 - self.scale * (d[1] - d[0]))

    def __repr__(self): return "edges"


class FocusWave(Node):
    """Angular wave around a focal point: rays / petals."""

    def __init__(self, rng, scene):
        self.i = int(rng.integers(len(scene.foci)))
        self.k = int(rng.integers(2, 9))
        self.phase = rng.uniform(0, math.pi)

    def eval(self, scene, X, Y):
        fx, fy = scene.foci[self.i]
        return np.sin(self.k * np.arctan2(Y - fy, X - fx) + self.phase)

    def __repr__(self): return f"rays(f{self.i},{self.k})"


# --- Unary value operators ------------------------------------------------------------------

class Unary(Node):
    arity = 1

    def __init__(self, rng, scene, child):
        self.children = (child,)


class Well(Unary):
    def eval(self, scene, X, Y): return _well(self.children[0].eval(scene, X, Y))

    def __repr__(self): return f"well({self.children[0]})"


class Tent(Unary):
    def eval(self, scene, X, Y): return _tent(self.children[0].eval(scene, X, Y))

    def __repr__(self): return f"tent({self.children[0]})"


class Sin(Unary):
    def __init__(self, rng, scene, child):
        super().__init__(rng, scene, child)
        self.phase = rng.uniform(0, math.pi)
        self.freq = rng.uniform(1.0, 6.0)

    def eval(self, scene, X, Y):
        return np.sin(self.phase + self.freq * self.children[0].eval(scene, X, Y))

    def __repr__(self): return f"sin({self.freq:.1f}*{self.children[0]})"


class Negative(Unary):
    def eval(self, scene, X, Y): return -self.children[0].eval(scene, X, Y)

    def __repr__(self): return f"-({self.children[0]})"


class Discretize(Unary):
    """Quantise into a few flat bands: posterised steps, crisp contours."""

    def __init__(self, rng, scene, child):
        super().__init__(rng, scene, child)
        self.levels = int(rng.integers(2, 7))

    def eval(self, scene, X, Y):
        v = (self.children[0].eval(scene, X, Y) + 1) / 2
        return np.round(v * self.levels) / self.levels * 2 - 1

    def __repr__(self): return f"steps{self.levels}({self.children[0]})"


# --- Coordinate transforms (evaluate the subtree at different coordinates) -----------------

class Rotate(Unary):
    def __init__(self, rng, scene, child):
        super().__init__(rng, scene, child)
        self.a = rng.uniform(0, 2 * math.pi)

    def eval(self, scene, X, Y):
        c, s = math.cos(self.a), math.sin(self.a)
        return self.children[0].eval(scene, c * X - s * Y, s * X + c * Y)

    def __repr__(self): return f"rot({self.children[0]})"


class Mirror(Unary):
    """Reflect across a random line through the origin: bilateral symmetry."""

    def __init__(self, rng, scene, child):
        super().__init__(rng, scene, child)
        a = rng.uniform(0, math.pi)
        self.n = (math.cos(a), math.sin(a))

    def eval(self, scene, X, Y):
        nx, ny = self.n
        d = X * nx + Y * ny
        side = np.minimum(d, 0)  # fold the positive half onto the negative half
        return self.children[0].eval(scene, X - 2 * (d - side) * nx, Y - 2 * (d - side) * ny)

    def __repr__(self): return f"mirror({self.children[0]})"


class Kaleido(Unary):
    """n-fold dihedral symmetry around a centre (origin or a focal point)."""

    def __init__(self, rng, scene, child):
        super().__init__(rng, scene, child)
        self.n = int(rng.choice([3, 4, 5, 6, 8, 12]))
        self.center = (0.0, 0.0) if rng.uniform() < 0.6 else tuple(
            scene.foci[int(rng.integers(len(scene.foci)))])

    def eval(self, scene, X, Y):
        cx, cy = self.center
        r = np.hypot(X - cx, Y - cy)
        sector = 2 * math.pi / self.n
        t = np.mod(np.arctan2(Y - cy, X - cx), sector)
        t = np.abs(t - sector / 2)  # mirror inside each sector
        return self.children[0].eval(scene, cx + r * np.cos(t), cy + r * np.sin(t))

    def __repr__(self): return f"kaleido{self.n}({self.children[0]})"


class Torus(Unary):
    """Tile the plane: repeat the subtree on a k x k grid."""

    def __init__(self, rng, scene, child):
        super().__init__(rng, scene, child)
        self.k = rng.uniform(1.5, 4.0)

    def eval(self, scene, X, Y):
        wrap = lambda v: np.mod(v * self.k + 1, 2) - 1
        return self.children[0].eval(scene, wrap(X), wrap(Y))

    def __repr__(self): return f"tile({self.children[0]})"


class Polar(Unary):
    """Evaluate the subtree in polar coordinates: straight lines become rings and spokes."""

    def eval(self, scene, X, Y):
        r = np.hypot(X, Y) * 2 / math.sqrt(2) - 1
        t = np.arctan2(Y, X) / math.pi
        return self.children[0].eval(scene, r, t)

    def __repr__(self): return f"polar({self.children[0]})"


# --- Binary / ternary / select ---------------------------------------------------------------

class Binary(Node):
    arity = 2

    def __init__(self, rng, scene, a, b):
        self.children = (a, b)

    def both(self, scene, X, Y):
        return (c.eval(scene, X, Y) for c in self.children)


class Sum(Binary):
    def eval(self, scene, X, Y):
        a, b = self.both(scene, X, Y)
        return (a + b) / 2

    def __repr__(self): return f"({self.children[0]} + {self.children[1]})"


class Product(Binary):
    def eval(self, scene, X, Y):
        a, b = self.both(scene, X, Y)
        return a * b

    def __repr__(self): return f"({self.children[0]} * {self.children[1]})"


class Max(Binary):
    def eval(self, scene, X, Y):
        a, b = self.both(scene, X, Y)
        return np.maximum(a, b)

    def __repr__(self): return f"max({self.children[0]}, {self.children[1]})"


class Min(Binary):
    def eval(self, scene, X, Y):
        a, b = self.both(scene, X, Y)
        return np.minimum(a, b)

    def __repr__(self): return f"min({self.children[0]}, {self.children[1]})"


class Mod(Binary):
    def eval(self, scene, X, Y):
        a, b = self.both(scene, X, Y)
        with np.errstate(divide="ignore", invalid="ignore"):
            return _clip(np.where(np.abs(b) > 1e-3, np.mod(a, b), 0))

    def __repr__(self): return f"({self.children[0]} mod {self.children[1]})"


class Mix(Node):
    arity = 3

    def __init__(self, rng, scene, w, a, b):
        self.children = (w, a, b)

    def eval(self, scene, X, Y):
        w, a, b = (c.eval(scene, X, Y) for c in self.children)
        w = (w + 1) / 2
        return w * a + (1 - w) * b

    def __repr__(self): return f"mix({', '.join(map(repr, self.children))})"


class IfLess(Node):
    """Hard-edged select: where(a < b, then, else)."""
    arity = 4

    def __init__(self, rng, scene, a, b, then, other):
        self.children = (a, b, then, other)

    def eval(self, scene, X, Y):
        a, b, t, o = (c.eval(scene, X, Y) for c in self.children)
        return np.where(a < b, t, o)

    def __repr__(self): return f"if({self.children[0]} < {self.children[1]})"


# --------------------------------------------------------------------------------------------
# Generation
# --------------------------------------------------------------------------------------------

LEAVES = [(VarX, 1), (VarY, 1), (Const, 0.5), (FocusDist, 2), (NearestFocus, 1.5),
          (CellEdges, 1), (FocusWave, 1.5)]
VALUE_OPS = [(Well, 1), (Tent, 1), (Sin, 2), (Negative, 0.5), (Discretize, 1),
             (Sum, 2), (Product, 2), (Max, 1), (Min, 1), (Mod, 0.7), (Mix, 1.5), (IfLess, 1)]
TRANSFORMS = [(Rotate, 1), (Mirror, 1), (Kaleido, 1.5), (Torus, 1), (Polar, 0.7)]

REUSE_PROBABILITY = 0.2    # chance a child is an already-built node (shared subexpression)
MAX_TRANSFORMS = 3         # transforms compound quickly; keep images readable


def _pick(rng, weighted):
    classes, weights = zip(*weighted)
    w = np.array(weights, dtype=float)
    return classes[rng.choice(len(classes), p=w / w.sum())]


def build(rng, scene, depth, pool, transforms_left):
    """Random formula of roughly `depth` levels. Transforms are likelier near the root so they
    shape large parts of the image; finished nodes go into `pool` for reuse (DAG sharing)."""
    if pool and depth < 4 and rng.uniform() < REUSE_PROBABILITY:
        return pool[int(rng.integers(len(pool)))]
    if depth <= 0:
        node = _pick(rng, LEAVES)(rng, scene)
        pool.append(node)
        return node

    if transforms_left > 0 and rng.uniform() < 0.12 + 0.05 * depth:
        cls = _pick(rng, TRANSFORMS)
        node = cls(rng, scene, build(rng, scene, depth - 1, pool, transforms_left - 1))
    else:
        cls = _pick(rng, VALUE_OPS)
        kids = [build(rng, scene, depth - 1 - int(rng.integers(0, 2)), pool, transforms_left)
                for _ in range(cls.arity)]
        node = cls(rng, scene, *kids)
    pool.append(node)
    return node


def _grid(size):
    xs = np.linspace(-1, 1, size)
    return np.meshgrid(xs, -xs)


def _is_interesting(values):
    """Reject near-flat images, images mostly clipped to one extreme, and images with only 2-3
    distinct tones (the contrast stretch would turn those into flat two-colour images).
    Mirrors isInteresting() in cpp/sketches/prism.cpp."""
    saturated = np.mean(np.abs(values) > 0.98)
    lo, hi = values.min(), values.max()
    if values.std() <= 0.12 or saturated >= 0.55 or hi - lo < 1e-3:
        return False
    counts, _ = np.histogram(values, bins=16, range=(lo, hi))
    return np.sum(counts >= 0.01 * values.size) >= 4


MAX_ATTEMPTS = 25


def generate(seed, size=RESOLUTION):
    """Returns (values in [-1,1] of shape (size, size), formula, scene).

    Candidate formulas are previewed at low resolution and rejected when degenerate; retries are
    derived from `seed`, so the result is still deterministic per seed. Values are then stretched
    so the image uses the whole palette.
    """
    for attempt in range(MAX_ATTEMPTS):
        rng = np.random.default_rng([seed, attempt])
        scene = Scene(rng)
        depth = int(rng.integers(5, 9))
        formula = build(rng, scene, depth, pool=[], transforms_left=MAX_TRANSFORMS)
        if _is_interesting(_clip(formula.eval(scene, *_grid(96)))):
            break

    values = _clip(formula.eval(scene, *_grid(size)))
    lo, hi = np.percentile(values, [2, 98])
    if hi - lo > 1e-6:
        values = np.clip((values - lo) / (hi - lo) * 2 - 1, -1, 1)
    return values, formula, scene


def create_image(seed=0, bg_color=(0, 0, 0), cmap=None):
    values, _, _ = generate(seed)
    cmap = cmap or plt.get_cmap("viridis")
    rgb = cmap((values + 1) / 2)[..., :3]
    buffer = io.BytesIO()
    plt.imsave(buffer, rgb, format="jpg")
    return buffer.getvalue()
