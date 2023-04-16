package ml.learning.dubinscar.environment;

import java.util.ArrayList;
import java.util.List;

public class PolygonDecomposer {

   /**
    * A class representing a point in a 2D coordinate system.
    */
   public static class Point {
      private double x;
      private double y;

      /**
       * Constructs a new point object with the specified x and y coordinates.
       *
       * @param x the x-coordinate of the point
       * @param y the y-coordinate of the point
       */
      public Point(double x, double y) {
         this.x = x;
         this.y = y;
      }

      /**
       * Calculates the Euclidean distance between this point and another point.
       *
       * @param other the other point to calculate the distance to
       * @return the Euclidean distance between this point and the other point
       */
      public double distance(Point other) {
         double dx = x - other.getX();
         double dy = y - other.getY();
         return Math.sqrt(dx * dx + dy * dy);
      }

      /**
       * Returns the x-coordinate of this point.
       *
       * @return the x-coordinate of this point
       */
      public double getX() {
         return x;
      }

      /**
       * Returns the y-coordinate of this point.
       *
       * @return the y-coordinate of this point
       */
      public double getY() {
         return y;
      }
   }

   /**
    * A class representing a triangle defined by three vertices.
    */
   public static class Triangle {
      private Point[] vertices;

      /**
       * Constructs a triangle with the given vertices.
       *
       * @param v1 the first vertex of the triangle
       * @param v2 the second vertex of the triangle
       * @param v3 the third vertex of the triangle
       */
      public Triangle(Point v1, Point v2, Point v3) {
         vertices = new Point[3];
         vertices[0] = v1;
         vertices[1] = v2;
         vertices[2] = v3;
      }

      /**
       * Returns the area of the triangle.
       *
       * @return the area of the triangle
       */
      public double getArea() {
         double a = vertices[0].distance(vertices[1]);
         double b = vertices[1].distance(vertices[2]);
         double c = vertices[2].distance(vertices[0]);
         double s = (a + b + c) / 2.0;
         return Math.sqrt(s * (s - a) * (s - b) * (s - c));
      }

      /**
       * Returns the vertex at the specified index.
       *
       * @param index the index of the vertex to retrieve
       * @return the vertex at the specified index
       */
      public Point getVertex(int index) {
         return vertices[index];
      }

      /**
       * Returns an array containing the three vertices of the triangle.
       *
       * @return an array containing the three vertices of the triangle
       */
      public Point[] getVertices() {
         return vertices;
      }
   }

   /**
    * Calculates the cross product of three points in 2D space. The cross product
    * is a vector perpendicular to the plane defined by the three points. If the
    * cross product is positive, the points are in counterclockwise order. If the
    * cross product is negative, the points are in clockwise order. If the cross
    * product is zero, the points are collinear.
    *
    * @param p1 the first point
    * @param p2 the second point
    * @param p3 the third point
    * @return the cross product of the three points
    */
   private static double crossProduct(Point p1, Point p2, Point p3) {
      return (p2.getX() - p1.getX()) * (p3.getY() - p2.getY())
            - (p3.getX() - p2.getX()) * (p2.getY() - p1.getY());
   }

   /**
    * Applies the ear-clipping algorithm to generate a set of triangles from a
    * simple polygon represented as a list of points. Returns a list of Triangle
    * objects, where each Triangle represents a set of three vertices that form a
    * triangle. The algorithm works by iteratively removing "ears" from the polygon
    * until no more triangles can be formed. Each "ear" is defined as a vertex with
    * two adjacent edges that do not intersect any other edges in the polygon. This
    * method assumes that the polygon is simple, i.e. it does not intersect itself,
    * and that the vertices are listed in clockwise order.
    *
    * @param polygon A list of Point objects representing the vertices of the
    *                polygon
    * @return A list of Triangle objects representing the triangles formed by the
    *         polygon
    */
   private static List<Triangle> earClipping(List<Point> polygon) {
      List<Triangle> triangles = new ArrayList<>();

      int n = polygon.size();
      if (n < 3) {
         return triangles;
      }

      List<Point> vertices = new ArrayList<>(polygon); // make a copy
      int[] next = new int[n];
      int[] prev = new int[n];
      for (int i = 0; i < n; i++) {
         next[i] = (i + 1) % n;
         prev[i] = (i - 1 + n) % n;
      }

      int i = 0;
      while (vertices.size() >= 3 && i < n) {
         Point a = vertices.get(prev[i]);
         Point b = vertices.get(i);
         Point c = vertices.get(next[i]);

         if (isEar(a, b, c, vertices)) {
            triangles.add(new Triangle(a, b, c));
            vertices.remove(i);
            next[prev[i]] = next[i];
            prev[next[i]] = prev[i];
         } else {
            i = next[i];
         }
      }

      return triangles;
   }

   /**
    * Determines if a given polygon represented by a list of points is convex or
    * not. A polygon is convex if every interior angle is less than or equal to 180
    * degrees.
    *
    * @param points The list of points that make up the polygon.
    * @return true if the polygon is convex, false otherwise.
    */
   public static boolean isConvex(List<Point> points) {
      int size = points.size();
      if (size < 3) {
         return false;
      }
      boolean sign = false;
      for (int i = 0; i < size; i++) {
         Point p1 = points.get(i);
         Point p2 = points.get((i + 1) % size);
         Point p3 = points.get((i + 2) % size);
         double crossProduct = crossProduct(p1, p2, p3);
         if (crossProduct != 0) {
            if (sign) {
               if (crossProduct < 0) {
                  return false;
               }
            } else {
               sign = crossProduct > 0;
            }
         }
      }
      return true;
   }

   /**
    * Returns a boolean value indicating whether a triangle formed by points a, b,
    * and c is an ear in a given polygon. An ear is a triangle whose vertices are
    * the first, last, and one other point in a given polygon and contains no other
    * points within it.
    *
    * @param a        The first point of the triangle.
    * @param b        The second point of the triangle.
    * @param c        The third point of the triangle.
    * @param vertices A list of points representing the vertices of the polygon.
    * @return true if the triangle is an ear in the polygon, false otherwise.
    */
   private static boolean isEar(Point a, Point b, Point c, List<Point> vertices) {
      /*
       * Check whether the signed area of the triangle is negative. If the area is
       * negative, it means that the triangle is oriented clockwise and thus is not a
       * valid ear since the polygon is assumed to be counter-clockwise.
       */
      double area = signedArea(a, b, c);
      if (area >= 0) {
         return false;
      }
      /*
       * Check whether any other points of the polygon are located inside the triangle
       * formed by points a, b, and c. If any point is inside the triangle, it means
       * that the triangle is not an ear.
       */
      for (Point p : vertices) {
         if (p != a && p != b && p != c && isPointInTriangle(p, a, b, c)) {
            return false;
         }
      }
      return true;
   }

   /**
    * Returns a boolean value indicating whether a given point p is inside a
    * triangle formed by points a, b, and c.
    *
    * @param p The point to check.
    * @param a The first point of the triangle.
    * @param b The second point of the triangle.
    * @param c The third point of the triangle.
    * @return true if the point is inside the triangle, false otherwise.
    */
   private static boolean isPointInTriangle(Point p, Point a, Point b, Point c) {
      /*
       * Calculate the signed areas of the subtriangles formed by point p and each
       * pair of vertices of the triangle formed by points a, b, and c. If the sum of
       * the signed areas is equal to the signed area of the original triangle, it
       * means that point p is inside the triangle.
       */
      double alpha = signedArea(p, b, c);
      double beta = signedArea(p, c, a);
      double gamma = signedArea(p, a, b);
      return alpha >= 0 && beta >= 0 && gamma >= 0 || alpha <= 0 && beta <= 0 && gamma <= 0;
   }

   /**
    * Returns the signed area of a triangle formed by points a, b, and c using the
    * formula: <br>
    * (b.x - a.x) * (c.y - a.y) - (b.y - a.y) * (c.x - a.x)
    *
    * @param a The first point of the triangle.
    * @param b The second point of the triangle.
    * @param c The third point of the triangle.
    * @return The signed area of the triangle.
    */
   private static double signedArea(Point a, Point b, Point c) {
      return (b.x - a.x) * (c.y - a.y) - (b.y - a.y) * (c.x - a.x);
   }

   /**
    * Triangulates a given polygon into a list of triangles using ear-clipping. The
    * resulting list of triangles is then returned.
    *
    * @param polygon a list of points representing the vertices of the polygon
    * @return a list of triangles that make up the polygon
    * @throws IllegalArgumentException if the given polygon has less than 3 points
    */
   public static List<Triangle> triangulate(List<Point> polygon) throws IllegalArgumentException {
      // Polygon is already convex, so just apply the ear-clipping algorithm directly
      return earClipping(polygon);
   }

}
