/**
 * Example animation code.
 */

var direction = 1;
if (tick > ticks / 2) {
    direction = -1;
}
var xTickStep = 0.1;
var yTickStep = 0.2;

scene.lights[0].y = scene.lights[0].y + yTickStep * direction;
scene.lights[0].x = scene.lights[0].x + xTickStep * direction;
