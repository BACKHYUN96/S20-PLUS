package com.s20plus.pixeltraffic;

import android.graphics.Bitmap;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import javax.imageio.ImageIO;

/** Asset-only desktop check using the small android.graphics adapters beside this file. */
public final class VehicleSheet {
 public static void main(String[] args)throws Exception {
  String[] types={"SEDAN","COUPE","SUV","TAXI","BUS","TRUCK"};
  String[] palettes={"CLASSIC","COLORFUL","PASTEL"};
  int cellWidth=152,cellHeight=280;
  BufferedImage sheet=new BufferedImage(cellWidth*6,cellHeight*6,BufferedImage.TYPE_INT_RGB);
  Graphics2D g=sheet.createGraphics();
  g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
  g.setFont(new Font(Font.MONOSPACED,Font.BOLD,12));
  Set<Integer> hashes=new HashSet<>();
  int[] starts={20,20,18,20,4,10},lengths={40,40,44,40,72,60};
  for(int palette=0;palette<3;palette++)for(int direction=0;direction<2;direction++)for(int type=0;type<6;type++) {
   Bitmap sprite=VehicleArt.create(type,direction==1,palette);
   if(sprite.image.getWidth()!=40||sprite.image.getHeight()!=80)throw new AssertionError("Asset size changed");
   int visible=0,hash=1;
   Set<Integer> colors=new HashSet<>();
   for(int y=0;y<80;y++)for(int x=0;x<40;x++) {
    int pixel=sprite.image.getRGB(x,y);hash=31*hash+pixel;
    if((pixel>>>24)!=0) {
     visible++;colors.add(pixel);
     if(x==0||x==39||y==0||y==79)throw new AssertionError("Clipped sprite outer border");
     if(y<starts[type]||y>=starts[type]+lengths[type])throw new AssertionError("Nominal vehicle length changed: "+types[type]);
    }
   }
   if(visible<400||colors.size()<14)throw new AssertionError("Missing body or lighting detail");
   if(!hashes.add(hash))throw new AssertionError("Duplicate type, direction or palette");
   int px=type*cellWidth,py=(palette*2+direction)*cellHeight;
   g.setColor(new Color(0x182537));g.fillRect(px,py,cellWidth,cellHeight);
   g.setColor(new Color(0x29394b));g.fillRect(px+16,py+30,120,240);
   g.drawImage(sprite.image,px+16,py+30,120,240,null);
   g.setColor(new Color(0xd4dfdf));g.drawString(types[type]+" / "+(direction==1?"REAR":"FRONT"),px+7,py+14);
   g.setColor(new Color(0x96adb6));g.drawString(palettes[palette],px+7,py+27);
  }
  g.dispose();ImageIO.write(sheet,"png",new File(args[0]));
  System.out.println("PASS: 36 distinct sprites; 40x80 dimensions, nominal lengths, visible body detail, transparent unclipped borders. Desktop asset sheet (not an Android screenshot): "+args[0]);
 }
}
