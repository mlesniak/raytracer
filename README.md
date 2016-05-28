[![Build Status](http://jenkins.mlesniak.com/buildStatus/icon?job=raytracer)](http://jenkins.mlesniak
.com/job/raytracer/)
[![Quality status](http://sonarqube.mlesniak.com/api/badges/gate?key=com.mlesniak:raytracer)](http://sonarqube.mlesniak.com/overview?id=com.mlesniak%3Araytracer)

    
    
# Introduction

A simple raytracer using nothing but plain Java, a few helper classes, linerar algebra and physics. Note that the 
current state of this project is :boom: hacky :boom:, i .e. the code is not yet refactored, structured or documented.

# Gallery

These images show progress and bugs while developing.

![Commit 1fd3495](gallery/image-1fd3495.png?raw=true)
![Commit 460f043](gallery/image-460f043.png?raw=true)
![Commit bc76514](gallery/image-bc76514.png?raw=true)
![Commit 70c56f1](gallery/image-70c56f1.png?raw=true)
![Commit 3b7f1a3](gallery/image-3b7f1a3.png?raw=true)
![Commit b08068b](gallery/image-b08068b.png?raw=true)
![Commit d5ba2cc](gallery/image-d5ba2cc.png?raw=true)
![Commit 72f62ba](gallery/image-72f62ba.png?raw=true)
![Commit 54d82c1](gallery/image-54d82c1.png?raw=true)
![Commit 9e0ce99](gallery/image-9e0ce99.png?raw=true)
![Commit 6ba2ab0](gallery/image-6ba2ab0.png?raw=true)
![Commit 0052291](gallery/image-0052291.png?raw=true)
![Commit e743764](gallery/image-e743764.png?raw=true)
![Commit fa5d1af](gallery/image-fa5d1af.png?raw=true)
![Commit 628ab9e](gallery/image.png-628ab9e.gif?raw=true)
![Commit 628ab9e](gallery/image.png-4aaa4c5.gif?raw=true)

# Quality

Clean code is important for me, and even when I hack around like (currently!) in this project,
a minimal level of quality is necessary, e.g. to come back after a few days and not be totally lost. 
Hence, as part of the Maven build process we automatically check the code quality using

- Checkstyle (in particular for source code formatting)
- FindBugs

The corresponding configuration files are stored in ```src/main/resrouces/codestyle```. If any of these tools emit a 
warning, the build fails.

In addition, we use SonarQube with its default profile to analyse the source. If **any** warning is emitted, the build 
fails, too. This is implemented by a very strict quality gate:
 
<img src="https://raw.githubusercontent.com/mlesniak/raytracer/master/gallery/strict-quality-gate.png" 
height="300" style="display:block; margin-left:auto; margin-right:auto;"/> 

# Design remarks

- Currently, Vector3D objects are *not* immutable since we need mutable objects with getter and setter
for the YAML parser. One solution would be to have Parser objects and a conversion function but I do not like to
have to object types with nearly the same purpose. One idea might be a ```lock``` state: after an object is locked, 
any call to a setter (even to unlock) throws an exception?

- I experiment in this project with with micro commits, that is a lot of commits even for small
    semantic independent changes.

# Animation support

By defining animation properties in a scene an animated .gif is generated instead of a .png. Add the 
```animation``` property in the scene

    animation:
      file: src/main/resources/scene/default.js
      ticks: 24
      # Duration in ms
      duration: 1000
      loop: true
      
and write JavaScript code to modify the scene on each tick, e.g.
      
      var direction = 1;
      if (tick > ticks / 2) {
          direction = -1;
      }
      var xTickStep = 0.1;
      var yTickStep = 0.2;
      var radiusStep = 0.05;
      scene.objects[2].radius = scene.objects[2].radius + radiusStep * direction;
      scene.lights[0].y = scene.lights[0].y + yTickStep * direction;
      scene.lights[0].x = scene.lights[0].x + xTickStep * direction;


# References

- An Introduction to Ray Tracing, Andrew S. Glassner et al., 1989, The Morgan Kaufmann Series in Computer Graphics
- [Ray-tracing formulas](http://www.ccs.neu.edu/home/fell/CSU540/programs/RayTracingFormulas.htm)


# Todo and planned features

- Phong Shading
- Multiple light sources (needs refactoring), and soft shadows, otherwise it's boring since we have no shadows anymore
- Reflections
- Procedural generation
- Box support
- Texture mapping
- Materials such as glass
- Antialiasing
- Global illumination
- Famous room rendering scene
- Better parallelization (and benchmarks)
- Support for external file formats (SketchUp? Blender?)
- Timed Unit-Test to find performance regressions? Will this work with TravisCI?
- ~~Shadows / Lightning~~
- ~~Plane as geometric object~~
- ~~Choose nearest pixel in view (not depending on order of scene objects)~~
- ~~negative z-axis goes into the scene~~
- ~~Parallelization~~
- ~~Implement standard FoV / Camera pattern~~
- ~~Gouraud Shading~~
- ~~White areas instead of black ones at the border or spheres?~~
- ~~Fix shadow bugs (See commit 0052291)~~
- ~~SonarQube integration with [sonarqube-badges](https://github.com/QualInsight/qualinsight-plugins-sonarqube-badges)~~
- ~~Animation support (might also ease debugging)~~

# License

Copyright (c) 2016 Michael Lesniak, licensed under the Apache License.
