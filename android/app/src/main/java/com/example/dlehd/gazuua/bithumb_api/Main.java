package com.example.dlehd.gazuua.bithumb_api;
import java.util.HashMap;

/**
 * 빗썸 api
 */
/**
 * 빗썸 api
 */
/**
 * 빗썸 api
 */
/**
 * 빗썸 api
 *//**
 * 빗썸 api
 */
/**
 * 빗썸 api
 */
/**
 * 빗썸 api
 *//**
 * 빗썸 api
 */
/**
 * 빗썸 api
 */
/**
 * 빗썸 api
 */
public class Main {
    public static void main(String args[]) {
		Api_Client api = new Api_Client("4ade8d802a5c56b9b6262205980f258e",
				"af646e371a79dcd261352af0ac1c217b");
	
		HashMap<String, String> rgParams = new HashMap<String, String>();
		rgParams.put("order_currency", "BTC");
		rgParams.put("payment_currency", "KRW");
	
	
		try {
		    String result = api.callApi("/info/balance", rgParams);
		    System.out.println(result);
		} catch (Exception e) {
		    e.printStackTrace();
		}
		
    }
}

