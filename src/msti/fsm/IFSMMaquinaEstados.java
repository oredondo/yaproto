/*
Copyright (c) 2012 Javier Ramirez-Ledesma
Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/

package msti.fsm;

public interface IFSMMaquinaEstados {

	public FSMEstado getEstadoSiguiente(FSMEstado estado);

	public FSMEstado getEstadoSiguiente(FSMEstado estado, FSMEvento evento);

	public FSMEstado getEstadoSiguiente(FSMEstado estado, FSMEvento evento, String guarda);

}
