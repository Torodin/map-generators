import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;

class Noise2D {
	public static void main(String[] args){
		perlinImage(380, 240, 5);
	}

	public static float[][] GeneratePerlinNoise(float[][] baseNoise, int octaveCount)
	{
	   int width = baseNoise.length;
	   int height = baseNoise[0].length;
	 
	   float[][][] smoothNoise = new float[octaveCount][][]; //an array of 2D arrays containing
	 
	   float persistance = 0.3f;
	 
	   //generate smooth noise
	   for (int i = 0; i < octaveCount; i++)
	   {
	       smoothNoise[i] = GenerateSmoothNoise(baseNoise, i);
	   }
	 
	    float[][] perlinNoise = GetEmptyArray(width, height);
	    float amplitude = 1.0f;
	    float totalAmplitude = 0.0f;
	 
	    //blend noise together
	    for (int octave = octaveCount - 1; octave >= 0; octave--)
	    {
	       amplitude *= persistance;
	       totalAmplitude += amplitude;
	 
	       for (int i = 0; i < width; i++)
	       {
	          for (int j = 0; j < height; j++)
	          {
	             perlinNoise[i][j] += smoothNoise[octave][i][j] * amplitude;
	          }
	       }
	    }
	 
	   //normalisation
	   for (int i = 0; i < width; i++)
	   {
	      for (int j = 0; j < height; j++)
	      {
	         perlinNoise[i][j] /= totalAmplitude;
	      }
	   }
	 
	   return perlinNoise;
	}
	
	public static float Interpolate(float x0, float x1, float alpha)
	{
	   return x0 * (1 - alpha) + alpha * x1;
	}
	
	public static float[][] GenerateSmoothNoise(float[][] baseNoise, int octave)
	{
	   int width = baseNoise.length;
	   int height = baseNoise[0].length;
	 
	   float[][] smoothNoise = GetEmptyArray(width, height);
	 
	   int samplePeriod = 1 << octave; // calculates 2 ^ k
	   float sampleFrequency = 1.0f / samplePeriod;
	 
	   for (int i = 0; i < width; i++)
	   {
	      //calculate the horizontal sampling indices
	      int sample_i0 = (i / samplePeriod) * samplePeriod;
	      int sample_i1 = (sample_i0 + samplePeriod) % width; //wrap around
	      float horizontal_blend = (i - sample_i0) * sampleFrequency;
	 
	      for (int j = 0; j < height; j++)
	      {
	         //calculate the vertical sampling indices
	         int sample_j0 = (j / samplePeriod) * samplePeriod;
	         int sample_j1 = (sample_j0 + samplePeriod) % height; //wrap around
	         float vertical_blend = (j - sample_j0) * sampleFrequency;
	 
	         //blend the top two corners
	         float top = Interpolate(baseNoise[sample_i0][sample_j0],
	            baseNoise[sample_i1][sample_j0], horizontal_blend);
	 
	         //blend the bottom two corners
	         float bottom = Interpolate(baseNoise[sample_i0][sample_j1],
	            baseNoise[sample_i1][sample_j1], horizontal_blend);
	 
	         //final blend
	         smoothNoise[i][j] = Interpolate(top, bottom, vertical_blend);
	      }
	   }
	 
	   return smoothNoise;
	}
	
	public static float[][] GenerateWhiteNoise(int width, int height, int seed)
	{
		Random random = new Random(seed);
	    float[][] noise = GetEmptyArray(width, height);
	 
	    for (int i = 0; i < width; i++)
	    {
	        for (int j = 0; j < height; j++)
	        {
	        	int x = width+i;
	        	int y = height+j;
	        	random.setSeed(random.nextLong() ^ x);
	        	random.setSeed(random.nextLong() ^ y);
	        	//System.out.println("X:"+x+" Y:"+y+" Res:"+seedWithOffset);
	        	//random.setSeed(seedWithOffset);
	            noise[i][j] = (float)random.nextDouble() % 1;
	        }
	    }
	 
	    return noise;
	}
	
	public static float[][] GetEmptyArray(int x, int y)
	{
		return new float[x][y];
	}
		
	public static BufferedImage perlinImage(int width, int height,int octave)
	{
		System.out.println("Generating noise map...");
		BufferedImage image = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = image.createGraphics();
		float[][] perlin = GeneratePerlinNoise(GenerateWhiteNoise(width,height,(int) System.currentTimeMillis()),octave);
		for(int x=0;x<width;x++)
		{
			for(int y=0;y<height;y++)
			{
				int grayscale = (int)(255f*perlin[x][y]);
				if(grayscale<0)
					grayscale = 0;
				if(grayscale>255)
					grayscale = 255;
				System.out.println("Grayscale ("+x+","+y+"):"+grayscale);
				graphics.setColor(new Color(grayscale,grayscale,grayscale));
				graphics.drawRect(x, y, 1, 1);
			}
		}
		graphics.dispose();
		File file = new File("noise.png");
		try {
			ImageIO.write(image, "png", file);
			System.out.println("Saved image");
		} catch (IOException e) {
			System.out.println("Error de escritura");
		}
		
		int material1 = 127;
		
		System.out.println("Generating colored map...");
		BufferedImage image2 = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics2 = image2.createGraphics();
		for(int x=0;x<width;x++){
			for(int y=0;y<height;y++){
				int colors = (int)(255f*perlin[x][y]);
				if(colors<0){
					colors = 0;
				}else if(colors>255){
					colors = 255;
				}
				if(colors<material1){
					colors = 0;
				}else{
					colors = 200;
				}
				//System.out.println("Colors ("+x+","+y+"):"+colors);
				graphics2.setColor(new Color(colors,colors,colors));
				graphics2.drawRect(x, y, 1, 1);
			}
		}
		graphics2.dispose();
		File file2 = new File("noiseColors.png");
		try {
			ImageIO.write(image2, "png", file2);
			System.out.println("Saved image");
		} catch (IOException e) {
			System.out.println("Error de escritura");
		}
		return image2;
	}
}

