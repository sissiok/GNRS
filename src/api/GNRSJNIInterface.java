
public class GNRSJNIInterface{

	static{
		/* requires install of libconfig++ from http://www.hyperrealm.com/libconfig/ */
		System.loadLibrary("config++");
		System.loadLibrary("gnrs-jni");
	}

	public GNRSJNIInterface(String configFile){
		init(configFile);
	}

	private native void init(String configFile);
	public native void insert(String GUID, String locators);
	//TODO - maps to insert; update semantics undefined
	public native void update(String GUID, String locators);
	public native String lookup(String GUID);

	public static void main(String[] args){

		if(args.length < 1){
			System.out.println("Usage: java GNRSJNIInterface <gnrs-client-config-file>");
			System.exit(0);	
		}
		GNRSJNIInterface gnrs = new GNRSJNIInterface(args[0]);
		String GUID = "ABCDE";
		String NA = "NA-12345";
		gnrs.insert(GUID, NA);
		String resp = gnrs.lookup(GUID);
		if(resp.equals(NA)){
			System.out.println("GNRS interface works!");
		}else{
			System.out.println("GNRS test failed!" + 
			" Expected locator '" + NA + "' for GUID '" 
			+ GUID + "', got back '" + resp + "'");
		}
	}
}
