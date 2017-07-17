/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.rip;

/**
 * Clase de configuración RIP
 *
 */
public class ConfiguracionRIP {

	/** Split horizon */

	enum SplitHorizon {
		NONE,
		SPLITHORIZON,
		POISONREVERSE
	};

	/* Modo del split horizon. Por defecto, normal */
	public volatile SplitHorizon splitHorizon = SplitHorizon.SPLITHORIZON;
	
	/** Interfaces que participan en RIP */
	
	/* Nombres de interfaces que participan en RIP (mandato IOS "network dir/pref") */
	public volatile String interfaces[] = new String[] { "eth0", "lo0" };
}
