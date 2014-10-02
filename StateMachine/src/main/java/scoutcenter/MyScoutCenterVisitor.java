package scoutcenter;

import scoutcenter.ScoutCenterParser.CountContext;
import scoutcenter.ScoutCenterParser.FleetContext;
import scoutcenter.ScoutCenterParser.ShipContext;



public class MyScoutCenterVisitor extends ScoutCenterBaseVisitor<Void> {

  @Override
  public Void visitFleet(FleetContext ctx) {
    System.out.println(ctx.getText());
    return super.visitFleet(ctx);
  }
  
  @Override
  public Void visitShip(ShipContext ctx) {
    System.out.println(ctx.getText());
    return super.visitShip(ctx);
  }
  
  @Override
  public Void visitCount(CountContext ctx) {
    System.out.println(ctx.getText());
    return super.visitCount(ctx);
  }
  
}
