import java.applet.*;
import java.awt.*;
import java.awt.image.*;

public final class Driver
   extends java.applet.Applet implements Runnable
{
	private Thread	 m_DUI = null;

	private Graphics m_Graphics;
	private Image offscreenImage;
	private Image cracked;
	private Image backdrop;
	private Image post;
	private Image[] signs;
	private Image[][] cars;
	private Image tree;
	private Image bush;
	private Graphics finishedGraphics;
	private Graphics offscreenGraphics;
	private Color[] road;
	private Color[] hill;
	private Color[] barricade;
	private Color[] wood;
	private Font updateFont;
	private Font startMouse;
	private Font tooSlow;
	private int[] xvals;
	private int[] yvals;
	private int[] xm;
	private int[] ym;
	private int[]	 track;
	private int[]	 trackZ;
	private int		 carPosition;
	private int		 facing;
	private int      roadFacing;
	private int		 trackLoc;
	private int		 speed=180;
	private boolean	 wrecked = false;
	private double[]	 zoom;
	private boolean  m_fAllLoaded = false;
	private int[]	 driverY;//place on track
	private int[]	 driverCar;//0=sportscar,etc
	private int[]	 driverDir;//0=SB,1=NB
	private int		 driverCount;
	private int[][]	 upcomingCars;
	private int[][]  roadSigns;
	private int[]	 roadType;
	private int[]	 trees;
	private int[]	 bushes;
	private int      facingZ;
	private long	 sleeper;
	public boolean	 running;
	public boolean	 autopilot=true;
	public int		 dimmer;
	private long	 startTime;
	private boolean	 gas;
	private boolean	 brake;
	private boolean	 left;
	private boolean  right;
	private Color[]	 tunnel;
	private Font	 title;
	private Color[]	 darkRoad;
	private Color[]	 darkLines;
	public Driver()
	{
		cars = new Image[3][2];
		driverY=new int[50];
		signs=new Image[3];
		driverCar=new int[50];
		driverDir=new int[50];
		upcomingCars = new int[21][2];
		running=false;
		road = new Color[21];
		darkRoad=new Color[21];
		darkLines=new Color[21];
		hill = new Color[21];
		barricade=new Color[21];
		tunnel=new Color[21];
		wood=new Color[21];
		xvals = new int[4];
		yvals = new int[4];
		xm = new int[6];
		ym = new int[6];
	}

	public String getAppletInfo()
	{
		return "Name: DUI\r\n" +
		       "Author: Derek L. Ramey, MCP\r\n" +
		       "Created with Microsoft Visual J++ Version 1.1";
	}


	public void init()
	{
        resize(400, 300);
		zoom = new double[26];
		for (int t=0;t<25;t++) {
			zoom[t] = 200.0/(t+1)-8;
		}
		offscreenImage = createImage(400,300);
		offscreenGraphics =offscreenImage.getGraphics();
		cracked=getImage(getCodeBase(),"cracked.gif");
		cars[0][0]=getImage(getCodeBase(),"car1f.gif");
		cars[0][1]=getImage(getCodeBase(),"car1b.gif");
		cars[1][0]=getImage(getCodeBase(),"car2f.gif");
		cars[1][1]=getImage(getCodeBase(),"car2b.gif");
		cars[2][0]=getImage(getCodeBase(),"car3f.gif");
		cars[2][1]=getImage(getCodeBase(),"car3b.gif");
		signs[0]=getImage(getCodeBase(),"banner1.gif");
		signs[1]=getImage(getCodeBase(),"banner2.gif");
		signs[2]=getImage(getCodeBase(),"banner3.gif");
		tree=getImage(getCodeBase(),"tree1.gif");
		bush=getImage(getCodeBase(),"tree2.gif");
		post=getImage(getCodeBase(),"post.gif");
		backdrop = createImage(800,600);
		drawBG(backdrop.getGraphics());	
		updateFont = new Font("TimesNewRoman", Font.BOLD, 12);
		startMouse=new Font("TimesNewRoman",Font.BOLD,18);
		tooSlow=new Font("TimesNewRoman",Font.BOLD,32);
		title=new Font("Serif",Font.BOLD|Font.ITALIC,42);
		for(int t=0;t<21;t++) {
			hill[t] = new Color(0,196-(t*5),0);
			road[t] = new Color(150-t*4,150-t*4,150-t*4);
			barricade[t]=new Color(255-t*6,255-t*6,255-t*6);
			wood[t]=new Color(150-t*4,100-t*3,50-t*2);
			tunnel[t]=new Color(75-t*3,50-t*2,50-t*2);
			darkRoad[t]=new Color(75-t*3,75-t*3,75-t*3);
			darkLines[t]=new Color(150-t*4,150-t*4,0);
		}
}

	public void destroy()
	{
	
	}

  	public void update(Graphics g)
	{
		paint(g);
	}

	public void paint(Graphics g)
	{
		g.drawImage(offscreenImage, 0, 0, this);
	}

	public void start()
	{
		if (m_DUI == null)
		{
			m_DUI = new Thread(this);
			m_DUI.start();
		}
	
	}
	
	public void stop()
	{
		if (m_DUI != null)
		{
			m_DUI.stop();
			m_DUI = null;
		}
	}

	public void drawBG(Graphics g) {
		g.setColor(Color.black);
		g.fillRect(0,0,800,600);
		g.setColor(Color.white);
		for(int t=0;t<250;t++) {
			int x=(int)Math.round(Math.random()*800);
			int y=(int)Math.round(Math.random()*600);
			g.drawLine(x,y,x,y);
		}
		g.setColor(Color.gray);
		for(int t=0;t<750;t++) {
			int x=(int)Math.round(Math.random()*800);
			int y=(int)Math.round(Math.random()*600);
			g.drawLine(x,y,x,y);
		}

	}
	public final void run()
	{
		if (!m_fAllLoaded)
		{

    		MediaTracker tracker = new MediaTracker(this);
            tracker.addImage(cracked, 0);
			tracker.addImage(post,0);
			tracker.addImage(tree,0);
			tracker.addImage(bush,0);
			for(int t=0;t<3;t++) {
				tracker.addImage(signs[t],0);
			}
			for(int z=0;z<3;z++) {
			for(int t=0;t<2;t++) {
				tracker.addImage(cars[z][t],0);
			}
			}
			try
			{
				tracker.waitForAll();
				m_fAllLoaded = !tracker.isErrorAny();
			}
			catch (InterruptedException e)
			{}			
			if (!m_fAllLoaded)
			{
			    stop();
			    m_Graphics.drawString("Error loading images!", 10, 40);
			    return;
			}	
        }		
		randomizeTrack();
		randomizeDrivers();
		startTime=System.currentTimeMillis();
		double incCount = 0;
		int state=0;
		while (true) {
			try
			{	
				sleeper = System.currentTimeMillis();
				int oface = facing;
				if ((running)||(autopilot)) {
					nextFrame(oface+roadFacing);
					incCount++;
					if (!autopilot) {
						if ((carPosition<-500)||(carPosition>500)) {
							wrecked = true;
						}				
						if (upcomingCars[1][0]!=999) {	
							int carD = driverDir[upcomingCars[1][0]];
							int xPos = -200+carD*400;
							int carW=300;
							if (driverCar[upcomingCars[1][0]]==2) {
								carW=150;
							}
							if ((carPosition < xPos+carW)&&(carD==0)) {
							wrecked=true;}
							if ((carPosition> xPos-carW)&&(carD==1)){
								wrecked=true;}
						}
						if (upcomingCars[1][1]!=999) {
							int carD = driverDir[upcomingCars[1][1]];
							int xPos = -200+carD*400;
							int carW=300;
							if (driverCar[upcomingCars[1][1]]==2) {
								carW=150;
							}
							if ((carPosition < xPos+carW)&&(carD==0)) {
							wrecked=true;}
							if ((carPosition> xPos-carW)&&(carD==1)){
								wrecked=true;}
						}
						if (upcomingCars[0][0]!=999) {	
							int carD = driverDir[upcomingCars[0][0]];
							int xPos = -200+carD*400;
							int carW=300;
							if (driverCar[upcomingCars[0][0]]==2) {
								carW=150;
							}
							if ((carPosition < xPos+carW)&&(carD==0)) {
							wrecked=true;}
							if ((carPosition> xPos-carW)&&(carD==1)){
								wrecked=true;}
						}
						if (upcomingCars[0][1]!=999) {
							int carD = driverDir[upcomingCars[0][1]];
							int xPos = -200+carD*400;
							int carW=300;
							if (driverCar[upcomingCars[0][1]]==2) {
								carW=150;
							}
							if ((carPosition < xPos+carW)&&(carD==0)) {
							wrecked=true;}
							if ((carPosition> xPos-carW)&&(carD==1)){
								wrecked=true;}
						}
					}
					if (wrecked == true) {					
						speed = 0;
						running=false;
					}
					if ((200.0/(speed+1)<incCount)&&(speed>0)) {
						incCount-=200.0/(speed+1);
						trackLoc++;
						roadFacing -= track[trackLoc];
						if (autopilot==true) {
							facing += track[trackLoc];}
						facingZ+=trackZ[trackLoc];
						if (trackLoc>5000) {
							running=false;
							speed=0;
						}
						carPosition+=(((facing+roadFacing)*speed)/20);
					}
					if (wrecked == false) {
					     updateDrivers();
					}
					else {									
						offscreenGraphics.drawImage(cracked,0,0,400,300,this);
					}
					
				}
				if (wrecked==true) {
						dimImage();
						dimmer++;
						if (dimmer > 50) {
						offscreenGraphics.setFont(startMouse);
						offscreenGraphics.setColor(Color.cyan);
						offscreenGraphics.drawString("Click mouse to restart",115,150);
						copyRight();
						}
					}
				if (autopilot) {				
					offscreenGraphics.setColor(Color.blue);
					offscreenGraphics.setFont(title);
					offscreenGraphics.drawString("3D Driver",120,60);
					offscreenGraphics.setFont(updateFont);
					offscreenGraphics.drawString("Version 1.2",175,75);
					offscreenGraphics.setColor(Color.white);
					offscreenGraphics.setFont(startMouse);
					offscreenGraphics.drawString("Click with the mouse to start",80,100);
					offscreenGraphics.setFont(updateFont);
					offscreenGraphics.drawString("A - Gas",180,130);
					offscreenGraphics.drawString("Z - Brake",175,150);
					offscreenGraphics.drawString("LEFT/RIGHT ARROWS - Turn",120,170);
					copyRight();
				}
				
				long now=80-(System.currentTimeMillis()-sleeper);
				repaint();
				if (now<10) {
					now=10;}
				Thread.sleep(now);
				if (left==true) {
					facing-=2;
					if (facing-roadFacing==-1) {
						facing=roadFacing;
					}
				}
				if (right==true) {
					facing+=2;
					if (facing-roadFacing==1) {
						facing=roadFacing;
					}
				}
				if (gas==true) {
					speed+=(300/(speed+20)+1);
					if (speed>200) {speed=200;}
				}
				if (brake==true) {
					speed-=(700/(speed+5)+2);
					if (speed<0) {speed=0;}
				}			
				
			}
			catch (InterruptedException e)
			{
				stop();
			}
		}
	}

	private final void nextFrame(int oface) {
		int origfacing = oface;
		int curPos = 0;
		int upcomingRoad[] = new int[26];
		int upcomingRoadZ[] = new int[26];
		upcomingRoad[0]=track[trackLoc];
		int curve=0;
		int elevcurve = 0;
		
		int bgX=facing*10;
		int bgY=facingZ*10;
		while (bgX<0) {
			bgX+=800;
		}
		while (bgX>800) {
			bgX-=800;
		}
		while (bgY<0) {
			bgY+=600;
		}
		while (bgY>600) {
			bgY-=600;
		}
		offscreenGraphics.clipRect(0,0,400,300);
		offscreenGraphics.drawImage(backdrop,-bgX,-bgY,this);
		offscreenGraphics.drawImage(backdrop,-bgX+800,-bgY,this);
		offscreenGraphics.drawImage(backdrop,-bgX+800,-bgY+600,this);
		offscreenGraphics.drawImage(backdrop,-bgX,-bgY+600,this);
		
		for(int t=1;t<21;t++) {
			curve += track[t+trackLoc];
			elevcurve += trackZ[t+trackLoc];
			for(int z=t;z<25;z++) {
				upcomingRoad[z] += curve;
				upcomingRoadZ[z] += elevcurve;
			}
		}
		for(int t=0;t<21;t++) {
			upcomingCars[t][0]=999;
			upcomingCars[t][1]=999;
		}
		for(int t=0;t<50;t++) {
			if ((driverY[t]>=trackLoc)&&(driverY[t]<=trackLoc+20)) {
				if (upcomingCars[driverY[t]-trackLoc][0]==999) {
					upcomingCars[driverY[t]-trackLoc][0]=t;
				}
				else {
					upcomingCars[driverY[t]-trackLoc][1]=t;
				}
			}
		}
		
		for (int t=20;t>0;t--) {		
			int offsetA = (upcomingRoad[t]-(carPosition/(t+1))) + 200 - origfacing*t;
			int offsetB = (upcomingRoad[t-1]-(carPosition/t)) + 200 - origfacing*t + origfacing;
			
			xvals[0] = offsetA-(int)(zoom[t]*4);
			xvals[1] = offsetA+(int)(zoom[t]*4);
			xvals[3] = offsetB-(int)(zoom[t-1]*4);
			xvals[2] = offsetB+(int)(zoom[t-1]*4);
			yvals[0] = 200+(int)(zoom[t]+(upcomingRoadZ[t]));
			yvals[1] = yvals[0];
			yvals[2] = 200+(int)(zoom[t-1]+(upcomingRoadZ[t-1]));
			yvals[3] = yvals[2];
							
			//drawCliff
			int oxsize = (int)(zoom[t]*4);
			int xsize = (int)(zoom[t-1]*4);	
			if (roadType[trackLoc+t]==0) {
				xm[0]=xvals[0];
				ym[0]=yvals[0];
				xm[1]=xvals[3];
				ym[1]=yvals[3];
				xm[2]=xm[1];
				ym[2]=300;
				xm[3]=xm[0]-(int)(300-yvals[0]);
				ym[3]=300;
				offscreenGraphics.setColor(hill[t]);
				offscreenGraphics.fillPolygon(xm,ym,4);
			}
			if (roadType[trackLoc+t]==1) {
				xm[0]=xvals[3];
				ym[0]=yvals[3];
				xm[1]=xvals[3];
				ym[1]=yvals[3]-(int)zoom[t-1];
				xm[2]=xvals[0];
				ym[2]=yvals[0]-(int)zoom[t];
				xm[3]=xvals[0];
				ym[3]=yvals[0];
				offscreenGraphics.setColor(barricade[t]);
				offscreenGraphics.fillPolygon(xm,ym,4);
			}
			if (roadType[trackLoc+t]==2) {
				xm[0]=xvals[3];
				ym[0]=yvals[3];
				xm[1]=xvals[3];
				ym[1]=yvals[3]-(int)(zoom[t-1]*5);
				xm[2]=xvals[0];
				ym[2]=yvals[0]-(int)(zoom[t]*5);
				xm[3]=xvals[0];
				ym[3]=yvals[0];
				offscreenGraphics.setColor(tunnel[t]);
				offscreenGraphics.fillPolygon(xm,ym,4);
			}
			//draw Road
			if (yvals[2]>yvals[0]) {
				xm[0]=offsetA-oxsize;
				xm[1]=offsetA+oxsize;
				xm[2]=offsetB+xsize;
				xm[3]=offsetB-xsize;
				float test=(float)(trackLoc+t)/100;
				if (test==Math.round(test)) {
					offscreenGraphics.setColor(new Color(255-t*2,0,0));
				}
				else{
					if (trackLoc+t>5000) {
						offscreenGraphics.setColor(new Color(0,255-t*4,0));
					}
					else {
						if (roadType[trackLoc+t]==1) {
							offscreenGraphics.setColor(wood[t]);
						}
						if (roadType[trackLoc+t]==2) {
							offscreenGraphics.setColor(darkRoad[t]);
						}
						if (roadType[trackLoc+t]==0) {							
							offscreenGraphics.setColor(road[t]);
						}
					}
				}
				offscreenGraphics.fillPolygon(xm,yvals,4);

				int lines= (int)((trackLoc+t));
				if ((float)lines/2 == lines/2) {
					xm[0] = offsetA-(int)(zoom[t]/8)-1;
					xm[1] = offsetA+(int)(zoom[t]/8)+1;
					xm[3] = offsetB-(int)(zoom[t-1]/8)-1;
					xm[2] = offsetB+(int)(zoom[t-1]/8)+1;
					ym[0] = yvals[0];
					ym[1] = yvals[0];
					ym[2] = yvals[2];
					ym[3] = yvals[2];
					if (roadType[trackLoc+t]==2) {
						offscreenGraphics.setColor(darkLines[t]);
					}
					else {
						offscreenGraphics.setColor(Color.yellow);
					}
					offscreenGraphics.fillPolygon(xm,ym,4);
				}
			}			
			//draw Mountain
			if (roadType[trackLoc+t]==0) {
				xm[0] = xvals[1];
				ym[0] = yvals[1];
				xm[1] = xm[0]+yvals[1];
				ym[1] = 0;
				xm[2] = 400;
				ym[2] = 0;
				xm[3] = 400;
				ym[3] = 300;
				xm[4]=xvals[2];
				ym[4]=300;
				xm[5]=xvals[2];
				ym[5]=yvals[2];
			offscreenGraphics.setColor(hill[t]);
			offscreenGraphics.fillPolygon(xm,ym,6);	
			}
			if (roadType[trackLoc+t]==1) {
				xm[0]=xvals[2];
				ym[0]=yvals[2];
				xm[1]=xvals[2];
				ym[1]=yvals[2]-(int)zoom[t-1];
				xm[2]=xvals[1];
				ym[2]=yvals[1]-(int)zoom[t];
				xm[3]=xvals[1];
				ym[3]=yvals[1];
				offscreenGraphics.setColor(barricade[t]);
				offscreenGraphics.fillPolygon(xm,ym,4);
			}
			if (roadType[trackLoc+t]==2) {
				xm[0]=xvals[2];
				ym[0]=yvals[2];
				xm[1]=xvals[2];
				ym[1]=yvals[2]-(int)(zoom[t-1]*5);
				xm[2]=xvals[1];
				ym[2]=yvals[1]-(int)(zoom[t]*5);
				xm[3]=xvals[1];
				ym[3]=yvals[1];
				offscreenGraphics.setColor(tunnel[t]);
				offscreenGraphics.fillPolygon(xm,ym,4);
				xm[0]=xvals[0];
				xm[1]=xvals[1];
				xm[2]=xvals[2];
				xm[3]=xvals[3];
				ym[0]=yvals[0]-(int)(zoom[t]*5);
				ym[1]=ym[0];
				ym[2]=yvals[2]-(int)(zoom[t-1]*5);
				ym[3]=ym[2];
				offscreenGraphics.fillPolygon(xm,ym,4);
				if (xvals[0]>0) {
					xm[0]=xvals[3];
					ym[0]=yvals[3]-(int)(zoom[t-1]*10);
					xm[1]=xm[0];
					ym[1]=300;
					xm[2]=xm[0]-(300-ym[0]);
					ym[2]=300;
					offscreenGraphics.setColor(hill[t]);
					offscreenGraphics.fillPolygon(xm,ym,3);
					ym[1]=yvals[3]-(int)(zoom[t-1]*5);
					ym[2]=ym[1];
					xm[2]=400;
					xm[3]=400;
					ym[3]=ym[0]-(400-xm[0]);
					offscreenGraphics.fillPolygon(xm,ym,4);
					xm[0]=xvals[2];
					xm[1]=xvals[2];
					xm[2]=400;
					xm[3]=400;
					ym[0]=ym[1];
					ym[1]=300;
					ym[2]=300;
					ym[3]=ym[0];
					offscreenGraphics.fillPolygon(xm,ym,4);
				}
			}
			if (upcomingCars[t][0]!= 999) {
				int car=upcomingCars[t][0];
				int XCar = offsetB+(xsize*(driverDir[car]-1))+(xsize/8);
				offscreenGraphics.drawImage(cars[driverCar[car]][driverDir[car]],XCar,yvals[2]-(xsize*3/4),xsize*3/4,xsize*3/4,this);				
			}
			if (upcomingCars[t][1]!= 999) {
				int car=upcomingCars[t][1];
				int XCar = offsetB+(xsize*(driverDir[car]-1))+(xsize/8);
				offscreenGraphics.drawImage(cars[driverCar[car]][driverDir[car]],XCar,yvals[2]-(xsize*3/4),xsize*3/4,xsize*3/4,this);
			}
			if (roadType[trackLoc+t]==0) {
				if (roadSigns[trackLoc+t][0]>0) {
					int xp=offsetB-(xsize*2);
					int yp=yvals[2]-(int)(zoom[t-1]*3);
					offscreenGraphics.drawImage(signs[roadSigns[trackLoc+t][0]-1],xp,yp,xsize,xsize/3,this);				
					offscreenGraphics.drawImage(post,xp+(xsize*19)/40,yp+(xsize/3),xsize/20,xsize,this);
				}
				if (roadSigns[trackLoc+t][1]>0) {
					int xp=xvals[2];
					int yp=yvals[2]-(int)(zoom[t-1]*6);
					offscreenGraphics.drawImage(signs[roadSigns[trackLoc+t][1]-1],xp,yp,xsize,xsize/3,this);				
					offscreenGraphics.drawImage(post,xp+(xsize*19)/40,yp+(xsize/3),xsize/20,xsize,this);
				}
				if (trees[trackLoc+t]>0) {
					int xp=offsetB+(int)(zoom[t-1]*(trees[trackLoc+t]+1));
					int yp=yvals[2]-(int)(zoom[t-1]*(trees[trackLoc+t]+6));
					if (xp<400) {
						offscreenGraphics.drawImage(tree,xp,yp,(int)(zoom[t-1]*4),(int)(zoom[t-1]*7),this);
					}
				}
				if (bushes[trackLoc+t]>0) {
					int xp=offsetB+(int)(zoom[t-1]*(bushes[trackLoc+t]+1));
					int yp=yvals[2]-(int)(zoom[t-1]*(bushes[trackLoc+t]+6));
					if (xp<400) {
						offscreenGraphics.drawImage(bush,xp,yp,(int)(zoom[t-1]*4),(int)(zoom[t-1]*7),this);
					}
				}
				if (trees[trackLoc+t]==-1) {
					int xp=offsetB-(int)(zoom[t-1]*9);
					int yp=yvals[2]-(int)(zoom[t-1]*4);
					if (xp>-(int)(zoom[t-1]*4)) {
						offscreenGraphics.drawImage(tree,xp,yp,(int)(zoom[t-1]*4),(int)(zoom[t-1]*7),this);
					}
				}
				if (bushes[trackLoc+t]==-1) {
					int xp=offsetB-(int)(zoom[t-1]*9);
					int yp=yvals[2]-(int)(zoom[t-1]*4);
					if (xp>-(int)(zoom[t-1]*4)) {
						offscreenGraphics.drawImage(bush,xp,yp,(int)(zoom[t-1]*4),(int)(zoom[t-1]*7),this);
					}
				}
			}
		}
		offscreenGraphics.setColor(Color.white);
		offscreenGraphics.setFont(updateFont);
		int time=(int)((System.currentTimeMillis()-startTime)/1000);
		offscreenGraphics.drawString("Checkpoints: "+(int)trackLoc/100+"  Speed: "+speed/2+" Driving Time: "+time,5,15);						
	}
	private void updateDrivers() {
		driverCount++;
		if (driverCount>3) {
			for(int t=0;t<50;t++) {
				driverCount=0;
				if (driverDir[t]==0) {
					driverY[t]--;
					if (driverY[t]==-1) {
						driverY[t]=4999;
					}
				}
				else {
					driverY[t]++;
					if (driverY[t]==5000) {
						driverY[t]=0;
					}
				}		
			}
		}
	}

	private void randomizeDrivers() {
		for(int t=0;t<50;t++) {
			driverDir[t] = (int)(Math.random()*2);
			driverY[t] = (int)(Math.random()*4990)+10;
			driverCar[t]=(int)(Math.random()*3);
		}
	}
	public boolean keyDown(Event evt, int key)
	{
		if (key == Event.LEFT)
		{	left=true;
			return true;
		}
		if (key == Event.RIGHT)
		{
			right=true;			
			return true;
		}
		if ((key == 'a')||(key=='A'))
		{
			gas=true;
			return true;
		}
		if ((key == 'z')||(key=='Z'))
		{	
			brake=true;
			return true;
		}
		return false;
	}
	public boolean keyUp(Event evt, int key)
	{
		if (key == Event.LEFT)
		{	left=false;
			return true;
		}
		if (key == Event.RIGHT)
		{
			right=false;			
			return true;
		}
		if ((key == 'a')||(key=='A'))
		{
			gas=false;
			return true;
		}
		if ((key == 'z')||(key=='Z'))
		{	
			brake=false;
			return true;
		}
		return false;
	}
	public boolean mouseDown(Event evt, int x, int y)
	{
		if (running==false) {
			newGame();
			running=true;
		}
		return true;
	}
	public void newGame() {		
		carPosition = 100;
		facing = 0;
		roadFacing=0;
		trackLoc = 0;
		speed=0;
		wrecked=false;
		autopilot=false;
		randomizeDrivers();
		randomizeTrack();
		startTime=System.currentTimeMillis();
		dimmer=0;
	}
	public void randomizeTrack() {
	
		track = new int[5100];
		trackZ = new int[5100];
		roadType=new int[5100];
		roadSigns=new int[5100][2];
		trees=new int[5100];
		bushes=new int[5100];

		for (int t=0;t<25;t++) {
			roadSigns[t*200+(int)(Math.random()*200)][(int)(Math.random()*2)]=(int)(Math.random()*3)+1;
		}
		for (int t=0;t<100;t++) {
			int loc=t*50+(int)(Math.random()*50);
			int val=0;
			switch ((int)(Math.random()*3)) {
			case 0:
				val=(int)(Math.random()*5)+1;
				break;
			case 1:
				val=1;
				break;
			case 2:
				val=-1;
				break;
			}
			trees[loc]=val;
		}
		for(int t=0;t<50;t++) {
			int loc=(int)(Math.random()*5000);
			int type=(int)(Math.random()*2)+1;
			int dist=1;
			if (type==1) {				
				dist=(int)(Math.random()*10)+5;
			}
			if (type==2) {
				dist=(int)(Math.random()*25)+10;
			}
			for(int z=0;z<dist;z++) {
				roadType[loc+z]=type;
			}
		}
		for (int t=0;t<100;t++) {
			int loc=t*50+(int)(Math.random()*50);
			int val=0;
			switch ((int)(Math.random()*3)) {
			case 0:
				val=(int)(Math.random()*5)+1;
				break;
			case 1:
				val=1;
				break;
			case 2:
				val=-1;
				break;
			}
			bushes[loc]=val;
		}
		for(int severity=1;severity<6;severity++) {
			int start;
			int end;
			int h;
			int v;
			switch(severity) {
			case 1:
				start=0;
				end=500;
				h=1;
				v=1;
				break;
			case 2:
				start=500;
				end=1500;
				h=2;
				v=1;
				break;
			case 3:
				start=1500;
				end=2500;
				h=3;
				v=2;
				break;
			case 4:
				start=2500;
				end=3500;
				h=4;
				v=2;
				break;
			default:
				start=3500;
				end=5000;
				h=5;
				v=3;
				break;
			}
			for (int t=0;t<(severity*30);t++) {
				int a = (int)(Math.random()*(end-start)+start);
				switch ((int)(Math.random()*10)) {
					case 6:
					case 7:
					case 0:	
						track[a] = (int)(Math.random()*(h*2+1))-h;
						break;
					case 1:
						track[a] = -h;
						track[a+1]=-(h*2);
						track[a+2]=-(h*3);
						track[a+3]=-(h*2);
						track[a+4]=-h;
						break;
					case 2:
						track[a] = h;
						track[a+1]=h*2;
						track[a+2]=h*3;
						track[a+3]=h*2;
						track[a+4]=h*1;
						break;
					case 8:
					case 9:
					case 3:	
						trackZ[a] = (int)((Math.random()*(v*2+1))-v)/2;
						break;
					case 4:
						trackZ[a] = -v;
						trackZ[a+1]=-(v*2);
						trackZ[a+2]=-(v);
						break;
					case 5:
						trackZ[a] = v;
						trackZ[a+1]=v*2;
						trackZ[a+2]=v;
						break;
				}
			}
		}
	}
	private void dimImage() {
		offscreenGraphics.setColor(Color.black);		
		for(int t=0;t<1000;t++) {
			int xd=(int)(Math.random()*400);
			int yd=(int)(Math.random()*300);
			offscreenGraphics.fillRect(xd,yd,4,4);
		}
	}
	private void copyRight() {
		Font g=new Font("TimesNewRoman",Font.BOLD,12);
		offscreenGraphics.setFont(g);
		offscreenGraphics.setColor(Color.white);
		offscreenGraphics.drawString("(C)1999 Derek L. Ramey",125,260);
		offscreenGraphics.drawString("All Rights Reserved.",125,270);
		offscreenGraphics.drawString("http://derekramey.virtualave.net",125,280);
		offscreenGraphics.drawString("Email: indybane@aol.com",125,290);
				
	}
	//{{DECLARE_CONTROLS
	//}}
}
