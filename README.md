[![Build Status](https://travis-ci.org/mlesniak/raytracer.svg?branch=master)](https://travis-ci.org/mlesniak/raytracer)

# Introduction

A simple raytracer. Note that the current state of this project is :boom: hacky :boom:, i.e. the code is not yet refactored, structured or 
documented.

# Gallery

These images show progress and bugs while developing.

![Commit 1fd3495](gallery/image-1fd3495.png?raw=true)
![Commit 460f043](gallery/image-460f043.png?raw=true)
![Commit bc76514](gallery/image-bc76514.png?raw=true)
![Commit 70c56f1](gallery/image-70c56f1.png?raw=true)
![Commit 3b7f1a3](gallery/image-3b7f1a3.png?raw=true)
![Commit b08068b](gallery/image-b08068b.png?raw=true)
![Commit d5ba2cc](gallery/image-d5ba2cc.png?raw=true)

# Quality

Clean code is important for me, and even when I hack around like (currently!) in this project,
a minimal level of quality is necessary, e.g. to come back after a few days and not be totally lost. 
Hence, as part of the build process we automatically check the code quality using

- Checkstyle (in particular for source code formatting)
- FindBugs

The corresponding configuration files are stored in ```src/main/resrouces/codestyle```. If any of these tools emit a 
warning, the build fails.

# Design remarks

- Currently, Vector3D objects are *not* immutable since we need mutable objects with getter and setter
for the YAML parser. One solution would be to have Parser objects and a conversion function but I do not like to
have to object types with nearly the same purpose. One idea might be a ```lock``` state: after an object is locked, 
any call to a setter (even to unlock) throws an exception?

# References

- An Introduction to Ray Tracing, Andrew S. Glassner et al., 1989, The Morgan Kaufmann Series in Computer Graphics
- [Ray-tracing formulas](http://www.ccs.neu.edu/home/fell/CSU540/programs/RayTracingFormulas.htm)


# Todo and planned features

- ~~Implement standard FoV / Camera pattern~~
- **Gouraud Shading**
- Phong Shading
- Shadows / Lightning
- Reflections
- Materials such as glass
- ~~Plane as geometric object~~
- Texture mapping
- Antialiasing
- Procedural generation
- Animation support
- Support for external file formats (SketchUp? Blender?)
- ~~Choose nearest pixel in view (not depending on order of scene objects)~~
- ~~negative z-axis goes into the scene~~
- ~~Parallelization~~
- Timed Unit-Test to find performance regressions? Will this work with TravisCI?

# License

Copyright (c) 2016 Michael Lesniak, licensed under the Apache License.
