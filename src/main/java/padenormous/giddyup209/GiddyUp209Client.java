package padenormous.giddyup209;

import net.fabricmc.api.ClientModInitializer;
import padenormous.giddyup209.client.FlightHUD;

public class GiddyUp209Client implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		FlightHUD.register();
	}
}