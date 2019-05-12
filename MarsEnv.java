import jason.asSyntax.*;
import jason.environment.Environment;
import jason.environment.grid.GridWorldModel;
import jason.environment.grid.GridWorldView;
import jason.environment.grid.Location;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.Random;
import java.util.logging.Logger;

public class MarsEnv extends Environment {

    public static final int GSize = 9; // grid size
    public static final int GARB  = 16; // garbage code in grid model 
	public static final int OBST  = 20; // garbage code in grid model 
	 
    public static final Term    ns = Literal.parseLiteral("next(slot)");
    public static final Term    pg = Literal.parseLiteral("pick(garb)");
    public static final Term    dg = Literal.parseLiteral("drop(garb)");
    public static final Term    bg = Literal.parseLiteral("burn(garb)");
    public static final Literal g1 = Literal.parseLiteral("garbage(r1)");
	public static final Literal g3 = Literal.parseLiteral("garbage(r3)");
	public static final Literal g4 = Literal.parseLiteral("garbage(r4)");
    public static final Literal g2 = Literal.parseLiteral("garbage(r2)");

    static Logger logger = Logger.getLogger(MarsEnv.class.getName());

    private MarsModel model;
    private MarsView  view;

    @Override
    public void init(String[] args) {
        model = new MarsModel();
        view  = new MarsView(model);
        model.setView(view);
        updatePercepts();
    }

    @Override
    public boolean executeAction(String ag, Structure action) {
        logger.info(ag+" doing: "+ action);
        try {
            if (action.equals(ns)) {
                model.nextSlot(ag);
            } else if (action.getFunctor().equals("move_towards")) {
				if( ag.equals("r1") )
				{
					if(model.r1Container < model.maxContainer)
					{
						 Location r1Loc = model.getAgPos(0);
						if (model.hasObject(GARB, r1Loc)) 
						{
							model.pickGarb();
						}
						else
						{
							model.nextSlot(ag);
						}
						
					}
					else
					{
						int x = (int)((NumberTerm)action.getTerm(0)).solve();
						int y = (int)((NumberTerm)action.getTerm(1)).solve();
						model.moveTowards(x,y, ag);
					}
				}
				else
				{
					int x = (int)((NumberTerm)action.getTerm(0)).solve();
					int y = (int)((NumberTerm)action.getTerm(1)).solve();
					//int x = 4;
					//int y = 4;
					model.moveTowards(x,y, ag);
				}
                
            } else if (action.equals(pg)) {
                model.pickGarb();
            } else if (action.equals(dg)) {
				//Thread.sleep(200);
                model.dropGarb();
            } else if (action.equals(bg)) {
                model.burnGarb();
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        updatePercepts();

        try {
            Thread.sleep(200);
        } catch (Exception e) {}
        informAgsEnvironmentChanged();
        return true;
    }

    /** creates the agents perception based on the MarsModel */
    void updatePercepts() {
        clearPercepts();

        Location r1Loc = model.getAgPos(0);
        Location r2Loc = model.getAgPos(1);
		Location r3Loc = model.getAgPos(2);
		Location r4Loc = model.getAgPos(3);

        Literal pos1 = Literal.parseLiteral("pos(r1," + r1Loc.x + "," + r1Loc.y + ")");
        Literal pos2 = Literal.parseLiteral("pos(r2," + r2Loc.x + "," + r2Loc.y + ")");
		Literal pos3 = Literal.parseLiteral("pos(r3," + r3Loc.x + "," + r3Loc.y + ")");
		Literal pos4 = Literal.parseLiteral("pos(r4," + r4Loc.x + "," + r4Loc.y + ")");

        addPercept(pos1);
        addPercept(pos2);
		addPercept(pos3);
		addPercept(pos4);

        if (model.hasObject(GARB, r1Loc)) {
            addPercept(g1);
        }
        if (model.hasObject(GARB, r2Loc)) {
            addPercept(g2);
        }
		if (model.hasObject(GARB, r3Loc)) {
            addPercept(g3);
        }
		if (model.hasObject(GARB, r4Loc)) {
            addPercept(g4);
        }
    }

    class MarsModel extends GridWorldModel {

        public static final int MErr = 10; // max error in pick garb
        int nerr; // number of tries of pick garb
        boolean r1HasGarb = false; // whether r1 is carrying garbage or not
		boolean r3HasGarb = false; // whether r3 is carrying garbage or not
		boolean r4HasGarb = false; // whether r4 is carrying garbage or not
		public int r1Container;
		int r3Container;
		int r4Container;
		int maxContainer = 2;
        Random random = new Random(System.currentTimeMillis());

        private MarsModel() {
            super(GSize, GSize, 4); // hány ágens a szám!

            // initial location of agents
            try {
                setAgPos(0, 0, 0);
				setAgPos(2, 5, 	6);
				setAgPos(3, 2, 5);
                Location r2Loc = new Location(GSize/2, GSize/2);
                setAgPos(1, r2Loc); //dokkolo
				
            } catch (Exception e) {
                e.printStackTrace();
            }

            // initial location of garbage
            add(GARB, 3, 0);
			add(GARB, 3, 1);
			add(GARB, 0, 0);
			add(GARB, 4, 2);
			add(GARB, 5, 1);
			add(GARB, 2, 7);
			add(GARB, 7, 4);
		
			
			add(OBST, 1, 2); // igy tudunk obstaclet hozzáadni, nem az igazi de müködik, garbagenek nézi elöször a cuccos ezért kell a remove garbage is
			remove(GARB, 1, 2);
			
			add(OBST, 7, 7); // igy tudunk obstaclet hozzáadni, nem az igazi de müködik, garbagenek nézi elöször a cuccos ezért kell a remove garbage is
			remove(GARB, 7, 7);
			add(OBST, 7, 6); // igy tudunk obstaclet hozzáadni, nem az igazi de müködik, garbagenek nézi elöször a cuccos ezért kell a remove garbage is
			remove(GARB, 7, 6);
			add(OBST, 6, 7); // igy tudunk obstaclet hozzáadni, nem az igazi de müködik, garbagenek nézi elöször a cuccos ezért kell a remove garbage is
			remove(GARB, 6, 7);
			add(OBST, 6, 6); // igy tudunk obstaclet hozzáadni, nem az igazi de müködik, garbagenek nézi elöször a cuccos ezért kell a remove garbage is
			remove(GARB, 6, 6);
			
			add(OBST, 8, 8); // igy tudunk obstaclet hozzáadni, nem az igazi de müködik, garbagenek nézi elöször a cuccos ezért kell a remove garbage is
			remove(GARB, 8, 8);
			
			
            //add(GARB, GSize-1, 0);
            //add(GARB, 1, 2);
            //add(GARB, 0, GSize-2);
            
        }

        void nextSlot(String ag) throws Exception {
			Random rand = new Random();
			Location r1 = getAgPos(0);
			Location r2 = getAgPos(1);
			Location r3 = getAgPos(2);
			Location r4 = getAgPos(3);
			int add_x = rand.nextInt(3)-1;
			int add_y = rand.nextInt(3)-1;
			//adding random garbage
			int percent = rand.nextInt(100);
			if( percent < 2)
			{
				int garb_x = rand.nextInt(9);
				int garb_y = rand.nextInt(9);
				if( !(garb_x == r1.x && garb_y == r1.y || garb_x == r2.x && garb_y == r2.y || garb_x == r3.x && garb_y == r3.y || garb_x == r4.x && garb_y == r4.y || garb_x == 1 && garb_y == 2 || garb_x == 7 && garb_y== 7 || garb_x == 6 && garb_y == 6 || garb_x == 6 && garb_y == 7  || garb_x == 7 && garb_y == 6 || garb_x == 8 && garb_y == 8)  )
				{
					logger.info("Garbage generated in:" + garb_x +" "+garb_y);
					add(GARB, garb_x, garb_y);
				}
			
			}
			if( ag.equals("r1") )
			{
				r1.x += add_x;
				r1.y += add_y;
				//szélek
				if (r1.x == getWidth() || r1.x == -1 || r1.y == getHeight() || r1.y == -1 ) 
				{
					r1.x -= add_x;
					r1.y -= add_y;
				}
				//ütközés esetén maradunk ahol voltunk
				if(r1.x == r2.x && r1.y == r2.y || r1.x == r3.x && r1.y == r3.y || r1.x == r4.x && r1.y == r4.y)
				{
					r1.x -= add_x;
					r1.y -= add_y;
				}
				else if(r1.x == 8 && r1.y == 8 || r1.x == 1 && r1.y == 2 || r1.x == 7 && r1.y == 7 || r1.x == 6 && r1.y == 6 || r1.x == 6 && r1.y == 7  || r1.x == 7 && r1.y == 6  ) // ütközés obstacleel
				{
					r1.x -= add_x;
					r1.y -= add_y;
				}
				
			}
            else if( ag.equals("r3")  )
			{
				r3.x += add_x;
				r3.y += add_y;
				
				//szélek
				if (r3.x == getWidth() || r3.x == -1 || r3.y == getHeight() || r3.y == -1 ) 
				{
					r3.x -= add_x;
					r3.y -= add_y;
				}
				/*if (r3.x == getWidth()) 
				{
					r3.x--;
				}
				if(r3.x == -1)
				{
					r3.x++;
				}
				if (r3.y == getHeight()) {
				    r3.y--;
				}
				if (r3.y == -1) {
				    r3.y++;
				}*/
				//ütközés esetén maradunk ahol voltunk
				if(r3.x == r2.x && r3.y == r2.y || r1.x == r3.x && r1.y == r3.y || r3.x == r4.x && r3.y == r4.y)
				{
					r3.x -= add_x;
					r3.y -= add_y;
				}
				else if(r3.x == 8 && r3.y == 8 || r3.x == 1 && r3.y == 2 || r3.x == 7 && r3.y == 7 || r3.x == 6 && r3.y == 6 || r3.x == 6 && r3.y == 7  || r3.x == 7 && r3.y == 6  ) // ütközés obstacleel
				{
					r3.x -= add_x;
					r3.y -= add_y;
				}
			}
			else if( ag.equals("r4")  )
			{
				r4.x += add_x;
				r4.y += add_y;
				
				//szélek
				if (r4.x == getWidth() || r4.x == -1 || r4.y == getHeight() || r4.y == -1 ) 
				{
					r4.x -= add_x;
					r4.y -= add_y;
				}
				//ütközés esetén maradunk ahol voltunk
				if(r4.x == r2.x && r4.y == r2.y || r4.x == r3.x && r4.y == r3.y || r1.x == r4.x && r1.y == r4.y)
				{
					r4.x -= add_x;
					r4.y -= add_y;
				}
				else if(r4.x == 8 && r4.y == 8 || r4.x == 1 && r4.y == 2 || r4.x == 7 && r4.y == 7 || r4.x == 6 && r4.y == 6 || r4.x == 6 && r4.y == 7  || r4.x == 7 && r4.y == 6  ) // ütközés obstacleel
				{
					r4.x -= add_x;
					r4.y -= add_y;
				}
			}
			setAgPos(0, r1);
			setAgPos(2, r3);
			setAgPos(3, r4);
			setAgPos(1, r2); // just to draw it in the view
			
			//random irányba menést ide
			
            
            
        }

        void moveTowards(int x, int y, String ag) throws Exception {
			Location oldr1 = getAgPos(0);
			Location oldr3 = getAgPos(2);
			Location oldr4 = getAgPos(3);
			Location r1 = getAgPos(0);
			Location r2 = getAgPos(1);
			Location r3 = getAgPos(2);
			Location r4 = getAgPos(3);
			if( ag.equals("r1") )
			{
				if (r1.x < x)
				{
					r1.x++;
				}
				else if (r1.x > x)
				{
					r1.x--;
				}
				if (r1.y < y)
				{
					r1.y++;
				}
				else if (r1.y > y)
				{
					r1.y--;
				}
				//ütközés detektálás
				if(r1HasGarb == true)
				{
					if(r1.x == r3.x && r1.y == r3.y || r1.x == r4.x && r1.y == r4.y)
					{
						r1.x = oldr1.x;
						r1.y = oldr1.y;
					}
					else if(r1.x == 8 && r1.y == 8 || r1.x == 1 && r1.y == 2 || r1.x == 7 && r1.y == 7 || r1.x == 6 && r1.y == 6 || r1.x == 6 && r1.y == 7  || r1.x == 7 && r1.y == 6  ) // ütközés obstacleel
					{
						r1.x = oldr1.x;
						r1.y = oldr1.y;
					}
				}
				
				
			}
			if( ag.equals("r3") )
			{
				if (r3.x < x)
				{
					r3.x++;
				}
				else if (r3.x > x)
				{
					r3.x--;
				}
				if (r3.y < y)
				{
					r3.y++;
				}
				else if (r3.y > y)
				{
					r3.y--;
				}
				//ütközés
				if(r3HasGarb == true)
				{
					if(r1.x == r3.x && r1.y == r3.y || r3.x == r4.x && r3.y == r4.y)
					{
						r3.x = oldr3.x;
						r3.y = oldr3.y;
					}
					else if(r3.x == 8 && r3.y == 8 || r3.x == 1 && r3.y == 2 || r3.x == 7 && r3.y == 7 || r3.x == 6 && r3.y == 6 || r3.x == 6 && r3.y == 7  || r3.x == 7 && r3.y == 6  ) // ütközés obstacleel
					{
						r3.x = oldr3.x;
						r3.y = oldr3.y;
					}
				}
			}
            if( ag.equals("r4") )
			{
				if (r4.x < x)
				{
					r4.x++;
				}
				else if (r4.x > x)
				{
					r4.x--;
				}
				if (r4.y < y)
				{
					r4.y++;
				}
				else if (r4.y > y)
				{
					r4.y--;
				}
				//ütközés
				if(r4HasGarb == true)
				{
					if(r4.x == r3.x && r4.y == r3.y || r1.x == r4.x && r1.y == r4.y)
					{
						r4.x = oldr4.x;
						r4.y = oldr4.y;
					}
					else if(r4.x == 8 && r4.y == 8 || r4.x == 1 && r4.y == 2 || r4.x == 7 && r4.y == 7 || r4.x == 6 && r4.y == 6 || r4.x == 6 && r4.y == 7  || r4.x == 7 && r4.y == 6  ) // ütközés obstacleel
					{
						r4.x = oldr4.x;
						r4.y = oldr4.y;
					}
				}
			}
			
            setAgPos(0, r1);
			setAgPos(2, r3);
			setAgPos(3, r4);
			setAgPos(1, getAgPos(1)); // just to draw it in the view
        }

        void pickGarb() {
            // r1 location has garbage
            if (model.hasObject(GARB, getAgPos(0))) {
                // sometimes the "picking" action doesn't work
                // but never more than MErr times
                if (random.nextBoolean() || nerr == MErr) {
                    remove(GARB, getAgPos(0));
                    nerr = 0;
					r1Container++;
                    r1HasGarb = true;
					
                } else {
                    nerr++;
                }
            }
			// r3 location has garbage
            if (model.hasObject(GARB, getAgPos(2))) {
                // sometimes the "picking" action doesn't work
                // but never more than MErr times
                if (random.nextBoolean() || nerr == MErr) {
                    remove(GARB, getAgPos(2));
                    nerr = 0;
                    r3HasGarb = true;
                } else {
                    nerr++;
                }
            }
			// r4 location has garbage
            if (model.hasObject(GARB, getAgPos(3))) {
                // sometimes the "picking" action doesn't work
                // but never more than MErr times
                if (random.nextBoolean() || nerr == MErr) {
                    remove(GARB, getAgPos(3));
                    nerr = 0;
                    r4HasGarb = true;
                } else {
                    nerr++;
                }
            }
        }
        void dropGarb() {
			Location r1 = getAgPos(0);
			Location r2 = getAgPos(1);
			Location r3 = getAgPos(2);
			Location r4 = getAgPos(3);
            if (r1HasGarb && r1.x == r2.x && r1.y == r2.y) {
				r1Container = 0;
                r1HasGarb = false;
				
                add(GARB, getAgPos(0));
            }
			if (r3HasGarb) {
                r3HasGarb = false;
                add(GARB, getAgPos(2));
            }
			if (r4HasGarb) {
                r4HasGarb = false;
                add(GARB, getAgPos(3));
            }
        }
        void burnGarb() {
            // r2 location has garbage
            if (model.hasObject(GARB, getAgPos(1))) {
                remove(GARB, getAgPos(1));
            }
        }
    }

    class MarsView extends GridWorldView {

        public MarsView(MarsModel model) {
            super(model, "IER HF - Takarito robotok", 600);
            defaultFont = new Font("Arial", Font.BOLD, 18); // change default font
            setVisible(true);
            repaint();
        }

        /** draw application objects */
        @Override
        public void draw(Graphics g, int x, int y, int object) {
			if(object == MarsEnv.GARB)
			{
				drawGarb(g, x, y);
			}
			else
			{
				drawObst(g, x, y);
			}
            /*switch (object) {
            case MarsEnv.GARB:
                drawGarb(g, x, y);
                break;
			case MarsEnv.OBST:
                drawObst(g, x, y);
                break;
            }*/
        }

        @Override
        public void drawAgent(Graphics g, int x, int y, Color c, int id) {
			if(id>=0)
			{
				String label = "R"+(id+1);
				c = Color.blue;
				if (id == 0) {
					c = Color.yellow;
					if (((MarsModel)model).r1HasGarb) {
						String add_to_label =" - G - " + Integer.toString(((MarsModel)model).r1Container);
						label += add_to_label;
						c = Color.orange;
					}
				}
				if (id == 2) {
					c = Color.yellow;
					if (((MarsModel)model).r3HasGarb) {
						label += " - G";
						c = Color.orange;
					}
				}
				if (id == 3) {
					c = Color.yellow;
					if (((MarsModel)model).r4HasGarb) {
						label += " - G";
						c = Color.orange;
					}
				}
				super.drawAgent(g, x, y, c, -1);
				if (id == 0 || id == 2 || id == 3) {
					g.setColor(Color.black);
				} else {
					g.setColor(Color.white);
				}
				super.drawString(g, x, y, defaultFont, label);
				repaint();
			}
            
        }

        public void drawGarb(Graphics g, int x, int y) {
            super.drawObstacle(g, x, y);
            g.setColor(Color.white);
            drawString(g, x, y, defaultFont, "G");
        }
		public void drawObst(Graphics g, int x, int y) {
            super.drawObstacle(g, x, y);
            g.setColor(Color.red);
            drawString(g, x, y, defaultFont, "O");
        }

    }
}
