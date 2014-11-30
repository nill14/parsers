package b5par;

import org.antlr.v4.runtime.tree.TerminalNode;

import scoutcenter.ScoutCenterBaseListener;
import scoutcenter.ScoutCenterParser.FleetItemContext;
import scoutcenter.ScoutCenterParser.PartyContext;
import scoutcenter.ScoutCenterParser.RelationsContext;
import scoutcenter.ScoutCenterParser.SectorContext;

public class ScoutCenterExtractor extends ScoutCenterBaseListener {

	@Override
	public void enterFleetItem(FleetItemContext ctx) {

		String shipClass = ctx.ShipClass().getText();
		String ship = ctx.Ship().getText();
		int count = Integer.parseInt(ctx.Count().getText());
		System.out.printf("%s %s %d", shipClass, ship, count);
	}
	
	@Override
	public void exitSector(SectorContext ctx) {
		String sectorName = ctx.SectorName().getText();
		String sectorShort = ctx.SectorShort().getText();
	}
	
	@Override
	public void enterParty(PartyContext ctx) {
		TerminalNode fightersOnly = ctx.FightersOnly();
		System.out.println(fightersOnly);
		
	}
	
	@Override
	public void enterRelations(RelationsContext ctx) {
		String ours = ctx.Relation(0).getText();
		String theirs = ctx.Relation(1).getText();
		
	}
}
