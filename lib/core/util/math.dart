
import 'dart:math';

int clamp(int value, int start, int end) {
  return max(start, min(value, end));
}
// double clamp(double value, double start, double end) {
//   return max(start, min(value, end));
// }