package scoutcenter;

import scoutcenter.ScoutCenterParser.SectorContext;


public class ScoutCenterExtractor extends ScoutCenterBaseListener {

  @Override
  public void enterSector(SectorContext ctx) {
    System.out.println(ctx.getText());
  }
  
  @Override
  public void exitSector(SectorContext ctx) {
    System.out.println(ctx.getText());
  }
  
}
