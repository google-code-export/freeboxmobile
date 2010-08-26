package org.madprod.freeboxmobile.mvv;

/**
*
* @author Olivier Rosello
* $Id$
* 
*/

// Thanks to whiler for the code
// See http://blogs.wittwer.fr/whiler/2010/08/19/audio-format-a-law-pcm-froyo/

// Nécessaire pour convertir les messages de la mevo de Free (wav - ulaw) qui ne sont plus lus par Froyo (Android 2.2) suite à un bug

public class AudioConverter
{
	private static short[] a2l = { -5504, -5248, -6016, -5760, -4480, -4224,
		  -4992, -4736, -7552, -7296, -8064, -7808, -6528, -6272, -7040, -6784,
		  -2752, -2624, -3008, -2880, -2240, -2112, -2496, -2368, -3776, -3648,
		  -4032, -3904, -3264, -3136, -3520, -3392, -22016, -20992, -24064, -23040,
		  -17920, -16896, -19968, -18944, -30208, -29184, -32256, -31232, -26112,
		  -25088, -28160, -27136, -11008, -10496, -12032, -11520, -8960, -8448,
		  -9984, -9472, -15104, -14592, -16128, -15616, -13056, -12544, -14080,
		  -13568, -344, -328, -376, -360, -280, -264, -312, -296, -472, -456, -504,
		  -488, -408, -392, -440, -424, -88, -72, -120, -104, -24, -8, -56, -40,
		  -216, -200, -248, -232, -152, -136, -184, -168, -1376, -1312, -1504,
		  -1440, -1120, -1056, -1248, -1184, -1888, -1824, -2016, -1952, -1632,
		  -1568, -1760, -1696, -688, -656, -752, -720, -560, -528, -624, -592,
		  -944, -912, -1008, -976, -816, -784, -880, -848, 5504, 5248, 6016, 5760,
		  4480, 4224, 4992, 4736, 7552, 7296, 8064, 7808, 6528, 6272, 7040, 6784,
		  2752, 2624, 3008, 2880, 2240, 2112, 2496, 2368, 3776, 3648, 4032, 3904,
		  3264, 3136, 3520, 3392, 22016, 20992, 24064, 23040, 17920, 16896, 19968,
		  18944, 30208, 29184, 32256, 31232, 26112, 25088, 28160, 27136, 11008,
		  10496, 12032, 11520, 8960, 8448, 9984, 9472, 15104, 14592, 16128, 15616,
		  13056, 12544, 14080, 13568, 344, 328, 376, 360, 280, 264, 312, 296, 472,
		  456, 504, 488, 408, 392, 440, 424, 88, 72, 120, 104, 24, 8, 56, 40, 216,
		  200, 248, 232, 152, 136, 184, 168, 1376, 1312, 1504, 1440, 1120, 1056,
		  1248, 1184, 1888, 1824, 2016, 1952, 1632, 1568, 1760, 1696, 688, 656,
		  752, 720, 560, 528, 624, 592, 944, 912, 1008, 976, 816, 784, 880, 848 };

	public static void free2pcm(byte[] inBuffer, int inByteOffset,
			byte[] outBuffer, int outByteOffset, int sampleCount, boolean bigEndian)
	  {
		free2pcm(inBuffer, inByteOffset, outBuffer, outByteOffset, sampleCount, bigEndian, 1);
	  }

	public static void free2pcm(byte[] inBuffer, int inByteOffset,
			byte[] outBuffer, int outByteOffset, int sampleCount, boolean bigEndian,
			int volume)
	{
		int shortIndex = outByteOffset;
		int alawIndex = inByteOffset;
		while (sampleCount > 0)
		{
			intToBytes16(a2l[inBuffer[alawIndex++] & 0xFF], outBuffer, shortIndex++, bigEndian, volume);
			shortIndex++;
			sampleCount--;
		}
	}
	

	public static void intToBytes16(int sample, byte[] buffer, int byteOffset,
			boolean bigEndian, int volume)
	{
		if (bigEndian)
		{
			buffer[byteOffset++] = (byte) ((sample >> 8) * volume);
			buffer[byteOffset--] = (byte) ((sample & 0xFF) * volume);
		} else
		{
			buffer[byteOffset++] = (byte) ((sample & 0xFF) * volume);
			buffer[byteOffset--] = (byte) ((sample >> 8) * volume);
		}
	}
}
