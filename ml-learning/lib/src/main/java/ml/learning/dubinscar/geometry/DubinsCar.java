package ml.learning.dubinscar.geometry;

public class DubinsCar {

   // Position and orientation
   private double x;
   private double y;
   private double theta;

   // Minimum turning radius
   private double r;
   
   private int speed;

   public DubinsCar(double x, double y, double theta, double r, int speed) {
       this.x = x;
       this.y = y;
       this.theta = theta;
       this.r = r;
       this.speed = speed;
   }

   public void update(double steeringAngle, double speed, double dt) {
       double turnRadius = speed / Math.abs(steeringAngle);

       if (turnRadius < r) {
           steeringAngle = Math.copySign(speed / r, steeringAngle);
           turnRadius = r;
       }

       double dTheta = speed * dt / turnRadius;
       double dx = turnRadius * (Math.sin(theta + dTheta) - Math.sin(theta));
       double dy = turnRadius * (Math.cos(theta) - Math.cos(theta + dTheta));

       x += dx;
       y += dy;
       theta += dTheta;

       if (theta > Math.PI) {
           theta -= 2 * Math.PI;
       } else if (theta < -Math.PI) {
           theta += 2 * Math.PI;
       }
   }

   public double getX() {
       return x;
   }

   public double getY() {
       return y;
   }

   public double getTheta() {
       return theta;
   }

   public double getRadius() {
       return r;
   }
   
   public int getSpeed() {
      return speed;
   }
}
