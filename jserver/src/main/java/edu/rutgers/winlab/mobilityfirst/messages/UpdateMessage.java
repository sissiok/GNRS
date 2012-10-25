/*
 * Mobility First GNRS Server
 * Copyright (C) 2012 Robert Moore and Rutgers University
 * All rights reserved.
 */
package edu.rutgers.winlab.mobilityfirst.messages;

import edu.rutgers.winlab.mobilityfirst.structures.GNRSRecord;

/**
 * @author Robert Moore
 *
 */
public class UpdateMessage extends AbstractMessage {
  private GNRSRecord[] originalRecords;
  private GNRSRecord[] replacementRecords;
}
