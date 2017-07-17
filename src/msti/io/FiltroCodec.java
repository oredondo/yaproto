/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.io;

import java.io.IOException;

import msti.io.mensaje.IMensajeCodecFactoria;
import msti.io.mensaje.IMensajeCodificador;
import msti.io.mensaje.IMensajeDecodificador;

public class FiltroCodec extends Filtro {

	protected IMensajeCodecFactoria factoriaCodec;
	private IMensajeCodificador codificador;
	private IMensajeDecodificador decodificador;

	public FiltroCodec(String nombre, IMensajeCodecFactoria factoriaCodec) {
		super(nombre);
		this.factoriaCodec = factoriaCodec;
	}
	@Override
	public void init() {
		// TODO Auto-generated method stub
		codificador = factoriaCodec.getCodificador();
		decodificador = factoriaCodec.getDecodificador();
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean mensajeRecibido(Sesion sesion, Lectura lectura) {
		boolean resultado = false;
		// TODO: en datagrama, si el origen del fragmento es diferente del anterior, ¿acumular las lecturas?
		try {
			decodificador.decodificar(sesion, lectura);
			resultado = true;
		}
		catch (IOException e) {
			// TODO: devolver una excepción por la cadena, usando la cadena de filtros
			e.printStackTrace();			
		}
		
		// invoca al superior (propaga evento a otros filtros)
		super.mensajeRecibido(sesion, lectura);
		
		return resultado;
	}


	@Override
	public void escribir(Sesion sesion, Escritura escritura) {
		// TODO Auto-generated method stub
		// Obtener outputstream...
		try {
			codificador.codificar(sesion, escritura);
		} catch (IOException e) {
			// TODO Tratar excepción por la cadena capturando desde la cadena de filtros
			e.printStackTrace();
		}
		escritura.setCodificada(true);
		
		// invoca al superior (propaga evento a otros filtros)
		super.escribir(sesion, escritura);

	}
	@Override
	public int getMaxInputBytes() {
		// TODO Auto-generated method stub
		return decodificador.getMaxBytes();
	}


}
