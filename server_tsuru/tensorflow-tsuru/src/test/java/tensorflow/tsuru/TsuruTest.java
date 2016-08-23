package tensorflow.tsuru;

import java.net.InetSocketAddress;

import tensorflow.tsuru.TsuruNameResolver.Tsuru;

public class TsuruTest {

	public static void main(String[] args) throws Exception {
		String appName = "tf-skeleton";
		
		// Endereço da API do Tsuru
		String apiUrl = "";
		// Token do Usuário
		String token = "";

		Tsuru tsuru = new Tsuru(apiUrl, token);
		InetSocketAddress[] units = tsuru.listUnitAddress(appName);
		System.out.println("Units: " + units.length);
		for (InetSocketAddress unit : units)
			System.out.println(unit);
	}

}
