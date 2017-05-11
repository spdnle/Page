import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class OS
{
	//In part 1, the page will always be size 8
	//Index of pageArray is considered its physical address and the address points to the Page objects
	private static ArrayList<Page> pageArray = new ArrayList<Page>();
	//index is VPN and element is PPN
	ArrayList<Integer> pageTable = new ArrayList<Integer>(); 
	
	public int timeUnit = 1;
	public int maxIndex = 2;
	ArrayList<TLB> TLBArray = new ArrayList<TLB>(maxIndex);
	
	private static int numPages;
	private static int numBytes;
	
	private static int totalBits;
	private static int totalBitsPower;
	private static int VPN;
	private static int offset;
	
	public double totalMiss = 0;
	
	public double getTotalMiss()
	{
		return totalMiss;
	}
	
	public int getPPN(int VPN) //gets the PPN in the PAGE TABLE using the corresponding VPN
	{
		return pageTable.get(VPN);
	}
	
	//number of pages = 8 = 2^3, then VPN = 3
	private static int VPNbits()
	{
		double power = 0;
		for(int i = 1; i <= 24; i++)
		{
			power = Math.pow(2, i);
			if(power >= numPages)
			{
				int VPNbits = i;
				return VPNbits;
			}
		}
		return 0;
	}
	
	//size of each page = 16 = 2^4, then offset = 4
	private static int offsetBits()
	{
		double power = 0;
		for(int i = 1; i <= 24; i++)
		{
			power = Math.pow(2, i);
			if(power >= numBytes)
			{
				int offsetBits = i;
				return offsetBits;
			}
		}
		return 0;
	}

	public static void totalBits()
	{
		totalBits = VPNbits() + offsetBits();
		totalBitsPower = (int)(Math.pow(2, totalBits)-1);
	}
	
	//offset = virtAddress & ((2^offsetbits)-1)
	public void getOffset(int virtAddress)
	{	
		int offsetBits = (int)(Math.pow(2, offsetBits())-1);
		offset = virtAddress & offsetBits;
	}
	
	//VPN = ( virtAddress & (((2^totalbits)-1) - ((2^offsetbits)-1)) ) >> offsetbits
	public void getVPN(int virtAddress)
	{
		int VPNbits = (totalBitsPower) - (int)(Math.pow(2,offsetBits())-1);
		VPN = virtAddress & VPNbits;
		VPN = VPN >> offsetBits();
	}
	
	public static String separateNumPages(String s)
	{
		String newString = "";
		for(int i = 0; i < s.length(); i++)
		{
			if((s.charAt(i) + "").equals(" "))
			{
				return newString;
			}
			else
			{
				newString = newString.concat(s.charAt(i) + "");
			}
		}
		return null;
	}
	
	public static String separateNumBytes(String s)
	{
		String newString = "";
		for(int i = s.indexOf(" ")+1; i < s.length(); i++)
		{
			newString = newString.concat(s.charAt(i) + "");
		}
		return newString;
	}
	
	public static String separateVPN(String s)
	{
		String newString = "";
		for(int i = 0; i < s.length(); i++)
		{
			if((s.charAt(i) + "").equals("-"))
			{
				return newString;
			}
			else
			{
				newString = newString.concat(s.charAt(i) + "");
			}
		}
		return null;
	}
	
	public static String separatePPN(String s)
	{
		String newString = "";
		for(int i = s.indexOf(">")+1; i < s.length(); i++)
		{
			newString = newString.concat(s.charAt(i) + "");
		}
		return newString;
	}
	
	//returns the byte stored at virtual address (inside Page class)
	//NO TLB CHANGES
//	public byte getDataAtVirtAddress(int virtAddress) throws Exception //FOR PART 1 AND 2
//	{
//		try
//		{
//			getOffset(virtAddress);
//			totalBits();
//			System.out.println("Offset: " + offset);
//			getVPN(virtAddress);
//			System.out.println("VPN: " + VPN);
//			int PPN = getPPN(VPN);
//			return pageArray.get(PPN).getData(offset);
//		}catch(Exception e)
//		{
//			throw new Exception("invalid offset", e);
//		}
//	}
	
	public byte getDataAtVirtAddress(int virtAddress) throws Exception //Changed for TLB changes
	{
		try
		{
			getOffset(virtAddress); //gets the offset from the virtual address
			totalBits();
//			System.out.println("Offset: " + offset);
			getVPN(virtAddress); //gets the VPN from the virtual address
//			System.out.println("VPN: " + VPN);
			
			/*
			 * Is the VPN is in Translation look-aside buffer?
			 * If yes: get the PPN from the corresponding VPN
			 * If no: Go to pageTable and find the PPN from the corresponding VPN
			 */
			boolean hit = false;
			for(int i = 0; i < TLBArray.size(); i++)
			{
				if(VPN == TLBArray.get(i).getVPN())
				{
//					hit = true;
//					System.out.println("HIT!");
					int PPN = TLBArray.get(i).getPPN(); //HIT, so get the PPN from TLB
					return pageArray.get(PPN).getData(offset);
				}
			}
			if(hit == false) //redundant, remove
			{
				int PPN = getPPN(VPN); //MISS, so get the PPN from the pageTable
				if(TLBArray.size() == maxIndex+1)
				{
					totalMiss++;
					Random rand = new Random();
					int index = rand.nextInt(maxIndex);
					TLBArray.remove(index);
					TLBArray.add(index, new TLB(VPN,pageTable.get(VPN)));
					TimeUnit.MILLISECONDS.sleep(timeUnit);
					return pageArray.get(PPN).getData(offset); 
				}
				else if(TLBArray.size() < maxIndex+1)
				{
					totalMiss++;
//					System.out.println("MISS!");
//					System.out.println("NOT MISS: " + "VPN=" + VPN + " + " + "TLB ENTRY=" + TLBArray.get(i).getVPN());
					TLBArray.add(new TLB(VPN,pageTable.get(VPN)));
					TimeUnit.MILLISECONDS.sleep(timeUnit);
					return pageArray.get(PPN).getData(offset);
				}
			}					
			
//			int PPN = getPPN(VPN); //gets the PPN from the VPN
//			return pageArray.get(PPN).getData(offset); //returns physical address PPN+offset
		}catch(Exception e)
		{
			throw new Exception("invalid offset", e);
		}
		return 0;
	}

	//RANDOM
	public byte getDataAtVirtAddressReplaceRandom(int virtAddress) throws Exception //FOR PART 3
	{
		try
		{
			getOffset(virtAddress); //gets the offset from the virtual address
			totalBits();
//			System.out.println("Offset: " + offset);
			getVPN(virtAddress); //gets the VPN from the virtual address
//			System.out.println("VPN: " + VPN);
			
			/*
			 * Is the VPN is in Translation look-aside buffer?
			 * If yes: get the PPN from the corresponding VPN
			 * If no: Go to pageTable and find the PPN from the corresponding VPN
			 */
			boolean hit = false;
//			System.out.println("TLB SIZE: " + TLBArray.size());
//			System.out.println("");
			for(int i = 0; i < TLBArray.size(); i++)
			{
				if(VPN == TLBArray.get(i).getVPN())
				{
//					System.out.println("NOT MISS: " + "VPN=" + VPN + " + " + "TLB ENTRY=" + TLBArray.get(i).getVPN());
//					hit = true;
					int PPN = TLBArray.get(i).getPPN(); //HIT, so get the PPN from TLB
					return pageArray.get(PPN).getData(offset);
				}
			}
//			System.out.println("HIT: " + hit);
			if(hit == false) //redundant, remove
			{
				int PPN = getPPN(VPN); //MISS, so get the PPN from the pageTable
				if(TLBArray.size() == maxIndex+1)
				{
					totalMiss++;
//					System.out.println("totalMiss: " + totalMiss);
//					System.out.println("MISS");
					Random rand = new Random();
					int index = rand.nextInt(maxIndex);
					TLBArray.remove(index);
					TLBArray.add(index, new TLB(VPN,pageTable.get(VPN)));
					TimeUnit.MILLISECONDS.sleep(timeUnit);
					return pageArray.get(PPN).getData(offset); 
				}
				else if(TLBArray.size() < maxIndex+1)
				{
					totalMiss++;
//					System.out.println("totalMiss: " + totalMiss);
//					System.out.println("MISS");
					TLBArray.add(new TLB(VPN,pageTable.get(VPN)));
					TimeUnit.MILLISECONDS.sleep(timeUnit);
					return pageArray.get(PPN).getData(offset);
				}
			}					
			
//			int PPN = getPPN(VPN); //gets the PPN from the VPN
//			return pageArray.get(PPN).getData(offset); //returns physical address PPN+offset
		}catch(Exception e)
		{
			throw new Exception("invalid offset", e);
		}
		return 0;
	}
	
	//LEAST RECENTLY USED
	public byte getDataAtVirtAddressReplaceLRU(int virtAddress) throws Exception //FOR EXTRA CREDIT
	{
		try
		{
			getOffset(virtAddress); //gets the offset from the virtual address
			totalBits();
//			System.out.println("Offset: " + offset);
			getVPN(virtAddress); //gets the VPN from the virtual address
//			System.out.println("VPN: " + VPN);
			
			/*
			 * Is the VPN is in Translation look-aside buffer?
			 * If yes: get the PPN from the corresponding VPN
			 * If no: Go to pageTable and find the PPN from the corresponding VPN
			 */
			boolean hit = false;
//			System.out.println("TLB SIZE: " + TLBArray.size());
//			System.out.println("");
			for(int i = 0; i < TLBArray.size(); i++)
			{
				if(VPN == TLBArray.get(i).getVPN())
				{
//					hit = true;
//					System.out.println("NOT MISS: " + "VPN=" + VPN + " + " + "TLB ENTRY=" + TLBArray.get(i).getVPN());
					TLB dummy = TLBArray.get(i); //first save the TLB entry that is going to be replaced
					TLBArray.remove(i); //remove the TLB at index i
					TLBArray.add(dummy); //add the TLB back at the end, signifying that it is the most recently used entry
					int PPN = TLBArray.get(i).getPPN(); //HIT, so get the PPN from TLB
					return pageArray.get(PPN).getData(offset);
				}
			}
//			System.out.println("HIT: " + hit);
			if(hit == false) //redundant, remove
			{
				int PPN = getPPN(VPN); //MISS, so get the PPN from the pageTable
				if(TLBArray.size() == maxIndex+1)
				{
					totalMiss++;
//					System.out.println("totalMiss: " + totalMiss);
//					System.out.println("MISS");
					TLBArray.remove(0); //removes first index, which is the least recently used entry
					TLBArray.add(new TLB(VPN,pageTable.get(VPN))); //adds a new TLB in the last index
					TimeUnit.MILLISECONDS.sleep(timeUnit);
					return pageArray.get(PPN).getData(offset); 
				}
				else if(TLBArray.size() < maxIndex+1)
				{
					totalMiss++;
//					System.out.println("totalMiss: " + totalMiss);
//					System.out.println("MISS!");
					TLBArray.add(new TLB(VPN,pageTable.get(VPN)));
					TimeUnit.MILLISECONDS.sleep(timeUnit);
					return pageArray.get(PPN).getData(offset);
				}
			}					
			
//			int PPN = getPPN(VPN); //gets the PPN from the VPN
//			return pageArray.get(PPN).getData(offset); //returns physical address PPN+offset
		}catch(Exception e)
		{
			throw new Exception("invalid offset", e);
		}
		return 0;
	}
	
	public Page getPage(int PPN)
	{
		return pageArray.get(PPN);
	}
	
	public OS(String filename)
	{
		try
		{
			FileReader fr = new FileReader(filename);
			BufferedReader br = new BufferedReader(fr);
			String line = "";
			int firstLine = 0;
//			System.out.println("About to read line.");
			while((line = br.readLine()) != null)
			{
				int count = 0;
//				System.out.println("Reading lines");
				//numPages numBytes
				if(firstLine == 0)
				{
//					System.out.println("First Line");
					String strNumPages = separateNumPages(line);
					String strNumBytes = separateNumBytes(line);
					numPages = Integer.parseInt(strNumPages);
					numBytes = Integer.parseInt(strNumBytes);
					firstLine++;
				}
				//VPN->PPN
				else if(line.contains("->") && (line.length() < 11))
				{
//					System.out.println("VPN->PPN");
					String strVPN = separateVPN(line);
					String strPPN = separatePPN(line);
					
					int VPN = Integer.parseInt(strVPN);
					int PPN = Integer.parseInt(strPPN);
					pageTable.add(VPN, PPN);
				}
				else
				{
					byte[] buf = new byte[numBytes];
					buf = line.getBytes();
					pageArray.add(new Page(buf,count));
					count++;
				}
			}
			br.close();
		}catch (Exception e)
		{
			System.out.println(e);
		}
	}
}
