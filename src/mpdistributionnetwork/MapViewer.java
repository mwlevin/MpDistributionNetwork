/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpdistributionnetwork;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import static javax.swing.Action.ACTION_COMMAND_KEY;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.Scrollable;
import javax.swing.Timer;
import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.JMapViewer;
import static org.openstreetmap.gui.jmapviewer.JMapViewer.MIN_ZOOM;
import org.openstreetmap.gui.jmapviewer.MapMarkerDot;
import org.openstreetmap.gui.jmapviewer.MemoryTileCache;
import org.openstreetmap.gui.jmapviewer.Tile;
import org.openstreetmap.gui.jmapviewer.TileController;
import org.openstreetmap.gui.jmapviewer.interfaces.ICoordinate;
import org.openstreetmap.gui.jmapviewer.interfaces.MapMarker;
import org.openstreetmap.gui.jmapviewer.interfaces.TileLoaderListener;
import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.OsmTileSource;

/**
 *
 * @author ml26893
 */
public class MapViewer extends JMapViewer
{
    private static final Point[] move = {new Point(1, 0), new Point(0, 1), new Point(-1, 0), new Point(0, -1)};


    

    private int time;
    
    private int scale;
    
    private Location mouseStart, mouseEnd;
    private boolean drawSelection;
    
    private Network network;
    
    public MapViewer(Network network, int viewWidth, int viewHeight)
    {
        this.network = network;
        
        setPreferredSize(new Dimension(viewWidth, viewHeight));
        

        scale = 1;
        drawSelection = false;
        
        setFont(new Font("Arial", Font.BOLD, 14));
        

  
    }
    

    
    

    
    
    
    public void setScale(int scale)
    {
        this.scale = scale;
    }
    
    public void setTime(int t)
    {
        this.time = t;
        repaint();
    }
    
    public int getTime()
    {
        return time;
    }
    
    public void center(Location n)
    {
        setDisplayPosition(new Point(getWidth()/2, getHeight()/2), n.getCoordinate(), getZoom());
    }
    

    
    
    public void recenter()
    {
        recenter(getZoom());
    }
    
    public void recenter(int zoom)
    {
        double minX = Integer.MAX_VALUE;
        double maxX = Integer.MIN_VALUE;
        double minY = Integer.MAX_VALUE;
        double maxY = Integer.MIN_VALUE;
        
        for(Location n : network.getNodes())
        {

            
            if(n.getX() < minX)
            {
                minX = n.getX();
            }
            
            if(n.getX() > maxX)
            {
                maxX = n.getX();
            }
            
            if(n.getY() < minY)
            {
                minY = n.getY();
            }
            
            if(n.getY() > maxY)
            {
                maxY = n.getY();
            }
            
        }
        
        double xdiff = maxX - minX;
        double ydiff = maxY - minY;
        
        minX -= xdiff*0.2;
        maxX += xdiff*0.2;
        
        minY -= ydiff*0.2;
        maxY += ydiff*0.2;
        
        double center_x = (maxX + minX)/2;
        double center_y = (maxY + minY)/2;
        
        setDisplayPosition(new Point(getWidth()/2, getHeight()/2), new Coordinate(center_y, center_x), zoom);
        repaint();
    }
    
    public void setZoomControlsVisible(boolean visible) {
        super.setZoomControlsVisible(visible);
    }
    
    protected void paintComponent(Graphics window) 
    {
        Graphics2D g = (Graphics2D)window;
        
        super.paintComponent(g);


        for(Node i : network.getNodes())
        {
            for(Link l : i.outgoing){
                paintLink(g, l);
            }
        }
        
        

        for(Location n : network.getNodes())
        {
            paintNode(g, n);
        }
        
        for(Location n : network.getNodes())
        {
            if(n instanceof FC)
            {
                paintNode(g, n);
            }
        }
        
        for(Location n : network.getDests())
        {
            paintNode(g, n);
        }
       
    }
    
    public void saveHighResScreenshot(final File file) throws Exception
    {

        Thread t = new Thread()
        {
            public void run()
            {
                int width = getWidth()*2;
                int height = getHeight()*2;
                MapViewer map2 = new MapViewer(network, width, height);
                map2.setSize(new Dimension(width, height));

                map2.setTime(getTime());
                map2.setZoom(getZoom());
                map2.setCenter(getCenter());
                map2.setZoom(getZoom()+1);
                map2.setScale(2);
                BufferedImage image = new BufferedImage(map2.getWidth(), map2.getHeight(), BufferedImage.TYPE_INT_ARGB);
                map2.setZoomControlsVisible(false);
                Graphics g = image.getGraphics();

                for(int i = 0; i < 60; i++)
                {
                    map2.print(g);
                    try
                    {
                        Thread.sleep(1000);
                    }
                    catch(Exception ex){}
                }

                map2.print(g);
                //g.setColor(Color.black);
                //g.drawRect(0, 0, image.getWidth()-1, image.getHeight()-1);

                int minx = image.getWidth();
                int miny = image.getHeight();
                int maxx = 0;
                int maxy = 0;

                for(Node n : network.getNodes())
                {


                    Point p = map2.getMapPosition(n.getLatitude(), n.getLongitude());

                    minx = (int)Math.min(minx, p.x-10);
                    miny = (int)Math.min(miny, p.y-10);
                    maxx = (int)Math.max(maxx, p.x+10);
                    maxy = (int)Math.max(maxy, p.y+10);

                }


                maxx = (int)Math.min(maxx, image.getWidth());
                maxy = (int)Math.min(maxy, image.getHeight());
                minx = (int)Math.max(minx, 0);
                miny = (int)Math.max(miny, 0);


                int xdiff = maxx - minx;
                int ydiff = maxy - miny;


                BufferedImage actual = new BufferedImage(xdiff, ydiff, BufferedImage.TYPE_INT_ARGB);
                g = actual.getGraphics();
                g.drawImage(image, -minx, -miny, image.getWidth(), image.getHeight(), null);
                g.setColor(Color.black);
                g.drawRect(0, 0, xdiff-1, ydiff-1);
                try
                {
                    ImageIO.write(actual, "png", file);
                }
                catch(Exception ex)
                {
                    ex.printStackTrace(System.err);
                }
                System.out.println("Complete");
            
            }
        };
        t.start();
    }

    
    private static final double shift_r = 0.00005;
    
    protected void paintLink(Graphics2D g, Link l)
    {
       
        if(true) return;
        
        Location[] coords = l.getCoordinates();

        if(coords.length < 2)
        {
            return;
        }

        g.setColor(Color.black);
        
        // link width
        g.setStroke(new BasicStroke(3 * scale));

        Location prev = coords[0];
        for(int i = 1; i < coords.length; i++)
        {
            Location next = coords[i];

            Point p_start = getMapPosition(prev.getCoordinate(), false);
            Point p_end = getMapPosition(next.getCoordinate(), false);

            g.draw(new Line2D.Float(p_start, p_end));

            prev = next;
        }
    }
    
    public void paintNode(Graphics2D g, Location n)
    {
        //if(! (n instanceof FC)) return;
        
        // radius
        

        Color fill = Color.white;
        int radius = 5;
        
        if(n instanceof FC){
            fill = new Color(237, 28, 36);
        }
        else if(n instanceof SC){
            fill = new Color(255, 201, 41);
        }
        else if(n instanceof DS){
            fill = new Color(34, 177, 76);
        }
        else if(n instanceof ZIP3){
            fill = Color.black;
            radius = 1;
        }
        
        
        int sizeH = (int)radius * scale;
        Point position = getMapPosition(n.getCoordinate(), false);
        int size = sizeH * 2;
        
        Color color = fill;
        if (color != null) {
            Graphics2D g2 = (Graphics2D) g;
            Composite oldComposite = g2.getComposite();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
            g2.setPaint(color);
            g.fillOval(position.x - sizeH, position.y - sizeH, size, size);
            g2.setComposite(oldComposite);
        }
        // color
        g.setColor(Color.black);
        g.drawOval(position.x - sizeH, position.y - sizeH, size, size);

    }
    
    public void paintText(Graphics g, String name, Point position, int radius) {

        if (name != null && g != null && position != null) {
            g.setColor(Color.DARK_GRAY);
            g.setFont(getFont());
            g.drawString(name, position.x+radius+2, position.y+radius);
        }
    }
    

}
