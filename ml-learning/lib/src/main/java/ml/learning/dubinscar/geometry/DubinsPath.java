package ml.learning.dubinscar.geometry;

import java.util.List;

public class DubinsPath {
   private final List<Waypoint> waypoints;

   public DubinsPath(List<Waypoint> waypoints) {
       this.waypoints = waypoints;
   }

   public List<Waypoint> getWaypoints() {
       return waypoints;
   }
}

