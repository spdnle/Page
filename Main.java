import java.util.Random;

public class Main
{
	private static enum accessCases {Random, Sequential, Mixed, RandomLRU, SequentialLRU, MixedLRU};
	private static enum sizeCases {Ten, Twenty, Thirty, Hundred};
	private static enum delayCases {FiveMS, TenMS};
	private static int numTestCases = 5;
	private static long[] runTimes = new long[numTestCases];
	private static double[] missRates = new double[numTestCases];
	
	public static double average(long[] nums) 
	{
		double avg = 0;
		
		for (int i = 0; i < nums.length; i++ ) {
			avg += (double) nums[i];
		}
		
		avg /= nums.length;
		
		return avg;
	}
	
	public static double standardDeviation(long[] nums) 
	{
		double stdDev = 0;
		double sum = 0;
		
		double average = average(nums);
		
		for (int i = 0; i < nums.length; i++ ) {
			sum += Math.pow(((double) nums[i] - average), 2.0);
		}
		
		sum /= nums.length;
		
		stdDev = Math.sqrt(sum);
		
		return stdDev;
	}
	
	public static double averageMissRate(double[] nums) 
	{
		double avg = 0;
	
		for (int i = 0; i < nums.length; i++ ) {
			avg += nums[i];
		}
		
		avg /= nums.length;
		
		return avg*100;
	}
	
	public static void changeSize(sizeCases sizeCases, OS os) 
	{
		switch(sizeCases) 
		{
		case Ten:
			os.maxIndex = 10;
			break;
		case Twenty:
			os.maxIndex = 20;
			break;
		case Thirty:
			os.maxIndex = 30;
			break;
		case Hundred:
			os.maxIndex = 100;
			break;
		}
		
	}
	
	public static double missRate(double totalLookUp, double totalMiss)
	{
		return totalMiss/totalLookUp;
	}
	
	public static void changeDelay(delayCases delayCases, OS os) 
	{
		switch(delayCases) 
		{
		case FiveMS:
			os.timeUnit = 5;
			break;
		case TenMS:
			os.timeUnit = 10;
			break;
		}
	}

	public static void main(String[] args) throws Exception {
		OS os = new OS("proj2_data_large.txt");
		Random rand = new Random();
		int randomIndex = 0;
		for (accessCases accessCases : accessCases.values()) 
		{
			for (sizeCases sizeCases : sizeCases.values()) 
			{		
				for (delayCases delayCases : delayCases.values()) 
				{
					System.out.println("\n");
					System.out.format("%25s %25s %25s %25s %25s%n", "accessCases", "sizeCases", "delayCases" ,"CaseNum", "Runtime");
					System.out.format("%25s %25s %25s %25s %25s%n", "-----------", "---------", "----------", "-------", "-------");
					
					for (int i = 0; i < numTestCases; i++) 
					{		
						long startTime = System.nanoTime();
						
						switch(accessCases) 
						{
						case Random:
							changeSize(sizeCases, os);
							changeDelay(delayCases, os);
							for(int j = 0; j < 1000; j++)
							{
								randomIndex = rand.nextInt(16777215);
								os.getDataAtVirtAddressReplaceRandom(randomIndex);
							}								
							break;
						case Sequential:
							changeSize(sizeCases, os);
							changeDelay(delayCases, os);
							randomIndex = rand.nextInt(16777215-1000); //get random number - 1000 because loop
							for(int j = randomIndex; j < randomIndex+1000; j++)
							{
								os.getDataAtVirtAddressReplaceRandom(j); // get data for random number and for every +1
							}								
							break;
						case Mixed:
							changeSize(sizeCases, os);
							changeDelay(delayCases, os);
							for(int j = 0; j < 500; j++) // for 500 loops
							{
								randomIndex = rand.nextInt(16777215); //get random number
								os.getDataAtVirtAddressReplaceRandom(randomIndex); // get data for random number
							}
							randomIndex = rand.nextInt(16777215-500);
							for(int j = randomIndex; j < randomIndex+500; j++)  
							{
								os.getDataAtVirtAddressReplaceRandom(j);
							}
							break;
						case RandomLRU:
							changeSize(sizeCases, os);
							changeDelay(delayCases, os);
							for(int j = 0; j < 1000; j++)
							{
								randomIndex = rand.nextInt(16777215); //get random number
								os.getDataAtVirtAddressReplaceLRU(randomIndex); //get data for random number
							}								
							break;
						case SequentialLRU:
							changeSize(sizeCases, os);
							changeDelay(delayCases, os);
							randomIndex = rand.nextInt(16777215-1000); //get random number - 1000 because loop
							for(int j = randomIndex; j < randomIndex+1000; j++)
							{
								os.getDataAtVirtAddressReplaceLRU(j); // get data for random number and for every +1
							}								
							break;
						case MixedLRU:
							changeSize(sizeCases, os);
							changeDelay(delayCases, os);
							for(int j = 0; j < 500; j++) // for 500 loops
							{
								randomIndex = rand.nextInt(16777215); //get random number
								os.getDataAtVirtAddressReplaceLRU(randomIndex); // get data for random number
							}
							randomIndex = rand.nextInt(16777215-500);
							for(int j = randomIndex; j < randomIndex+500; j++)  
							{
								os.getDataAtVirtAddressReplaceLRU(j);
							}
							break;
						}
						missRates[i] = missRate((double)1000,os.getTotalMiss());
						os.totalMiss = 0;
						os.TLBArray.clear();
						runTimes[i] = System.nanoTime() - startTime;
						System.out.format("%25s %25s %25s %25s %25s%n", accessCases, sizeCases, delayCases, i, runTimes[i]);
						
					}
					System.out.println("\n");
					System.out.format("%25s %25s %25s%n", "RunTimeMean", "StandardDeviation", "MissRateMean");
					System.out.format("%25s %25s %25s%n", "-----------", "-----------------", "------------");
					System.out.format("%25s %25s %25s%n", average(runTimes), standardDeviation(runTimes), averageMissRate(missRates));
					
					os.TLBArray.clear();
				}
			}
		}
	}
}
