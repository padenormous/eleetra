package padenormous.giddyup209;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GiddyUp209 implements ModInitializer {
	public static final String MOD_ID = "giddy-up-209";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing Giddy-Up-209 - Prepare for supersonic flight!");
	}
}