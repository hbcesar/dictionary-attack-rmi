/**
 * Master.java
 */
package br.inf.ufes.pp2016_01;

import java.rmi.Remote;

public interface Master extends Remote, SlaveManager, Attacker {
	// o mestre é um SlaveManager e um Attacker
}
