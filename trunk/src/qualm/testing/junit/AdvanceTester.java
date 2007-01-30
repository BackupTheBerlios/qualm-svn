package qualm.testing.junit;

import junit.framework.*;
import junit.textui.*;

import javax.sound.midi.*;

import qualm.*;

public class AdvanceTester extends TestCase {
  public AdvanceTester (String name) { super(name); }

  public static void main(String[] args) {
    TestRunner.runAndWait(new TestSuite(AdvanceTester.class));
  }

  public void testAdvance() throws Exception {
    // simple advancement.
  String adv1 = "<qualm-data>\n" + 
"  <title>ADV-1</title>\n" + 
"  <midi-channels>\n" + 
"    <channel num=\"1\">Kbd</channel>\n" + 
"  </midi-channels>\n" + 
"  <patches>\n" + 
"    <patch id=\"P1\" num=\"1\">Patch 1</patch>\n" + 
"    <patch id=\"P2\" num=\"2\">Patch 2</patch>\n" + 
"  </patches>\n" + 
"  <cue-stream>\n" + 
"    <global>\n" + 
"      <trigger><note-on channel=\"1\" note=\"c4\"/></trigger>\n" + 
"      <trigger reverse=\"yes\"><note-on channel=\"1\" note=\"b3\"/></trigger>\n" + 
"    </global>\n" + 
"    <cue song=\"1\" measure=\"1\">\n" + 
"      <events><program-change channel=\"1\" patch=\"P1\"/></events>\n" + 
"    </cue>\n" + 
"    <cue song=\"2\" measure=\"1\">\n" + 
"      <events><program-change channel=\"1\" patch=\"P2\"/></events>\n" + 
"    </cue>\n" + 
"  </cue-stream>\n" + 
"</qualm-data>\n";

    FakeMIDI fm = FakeMIDI.prepareTest(adv1);
    fm.addOutgoing((long)0, ShortMessage.NOTE_ON, 0, 60, 10);
    fm.addOutgoing((long)300, ShortMessage.NOTE_ON, 0, 59, 10); // ignored
    fm.addOutgoing((long)1500, ShortMessage.NOTE_ON, 0, 60, 10); // no change
    fm.addOutgoing((long)3000, ShortMessage.NOTE_ON, 0, 59, 10); // reverse
    fm.addOutgoing((long)4500, ShortMessage.NOTE_ON, 0, 59, 10); // reverse
    fm.run();
    java.util.ArrayList msgs = fm.receivedMessages();

    System.out.println("Number of msgs received == " + msgs.size());
    java.util.Iterator iter = msgs.iterator();
    while (iter.hasNext()) {
      System.out.println("   " + MidiMessageParser.messageToString((MidiMessage)iter.next()));
    }

    assertTrue(msgs.size() == 4);
    ShortMessage sm1 = new ShortMessage();
    sm1.setMessage(ShortMessage.PROGRAM_CHANGE,0,0,0);
    ShortMessage sm2 = new ShortMessage();
    sm2.setMessage(ShortMessage.PROGRAM_CHANGE,0,1,0);
    // since ShortMessage doesn't define a useful equals() method, we compare using print reps
    assertEquals(MidiMessageParser.messageToString((ShortMessage)msgs.get(0)),
		 MidiMessageParser.messageToString(sm1)); // setup first cue
    assertEquals(MidiMessageParser.messageToString((ShortMessage)msgs.get(1)),
		 MidiMessageParser.messageToString(sm2)); // patch advance
    assertEquals(MidiMessageParser.messageToString((ShortMessage)msgs.get(2)),
		 MidiMessageParser.messageToString(sm1)); // reverse to first
    assertEquals(MidiMessageParser.messageToString((ShortMessage)msgs.get(3)),
		 MidiMessageParser.messageToString(sm1)); // reverse to first (regenerates)
  }

}
