
public class Page
{
	private int VPN;
	private byte[] byteArray = new byte[16]; //Data stored in the page
	
	public Page(byte[] buf, int VPN)
	{
		byteArray = buf;
		this.VPN = VPN;
	}
	
	public int getVPN()
	{
		return VPN;
	}
	
	public byte getData(int offset) throws Exception
	{
		try
		{
			return byteArray[offset];
		}catch(Exception e)
		{
			if((offset > byteArray.length) || offset < 0)
			{
				throw new Exception("invalid offset", e);
			}
		}
		return 0;
	}
}
			
