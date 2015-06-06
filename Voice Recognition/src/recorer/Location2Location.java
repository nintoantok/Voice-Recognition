package recorer;


import javax.media.*;
import javax.media.datasink.*;
import javax.media.protocol.*;


public class Location2Location implements ControllerListener {

  /** Output of the Processor: the transcoded media. */
  protected DataSource source;

  /** Sink used to "write" out the transcoded media. */
  protected DataSink sink;

  /** Processor used to transcode the media. */
  protected Processor processor;

  /**
   * Model used in constructing the processor, and which specifies track
   * formats and output content type
   */
  protected ProcessorModel model;

  /** State the object is in. */
  protected int state;

  /** Location that the media will be "written" to. */
  protected MediaLocator sinkLocation;

  /** The rate of translation. */
  protected float translationRate;

  /** Process has failed. */
  public static final int FAILED = 0;

  /**
   * Processor is working but not finished. DataSink is yet to start.
   */
  public static final int TRANSLATING = 1;

  /** DataSink has started but not finished. */
  public static final int TRANSFERRING = 3;

  /** Transcoding and transfer is complete. */
  public static final int FINISHED = 4;

  /** String names for each of the states. More user friendly */
  private static final String[] STATE_NAMES = { "Failed", "Translating",
      "<UNUSED>", "Transferring", "Finished" };

  /**
   * Period (in milliseconds) between checks for the blocking transfer method.
   */
  public static final int WAIT_PERIOD = 50;

  /**
   * Wait an "indefinite" period of time for the transfer method to complete.
   * i.e., pass to transfer() if the user wishes to block till the process is
   * complete, regardless of how long it will take.
   */
  public static final int INDEFINITE = Integer.MAX_VALUE;

  /***************************************************************************
   * Construct a transfer/transcode object that transfers media from
   * sourceLocation to destinationLocation, transcoding the tracks as
   * specified by the outputFormats. The output media is to have a content
   * type of outputContainer and the process should (if possible) run at the
   * passed rate.
   **************************************************************************/
  Location2Location(MediaLocator sourceLocation,
      MediaLocator destinationLocation, Format[] outputFormats,
      ContentDescriptor outputContainer, double rate) {

    //////////////////////////////////////////////
    // Construct the processor for the transcoding
    //////////////////////////////////////////////
    state = TRANSLATING;
    sinkLocation = destinationLocation;
    try {
      if (sourceLocation == null)
        model = new ProcessorModel(outputFormats, outputContainer);
      else
        model = new ProcessorModel(sourceLocation, outputFormats,
            outputContainer);
      processor = Manager.createRealizedProcessor(model);
    } catch (Exception e) {
      state = FAILED;
      return;
    }

    translationRate = processor.setRate((float) Math.abs(rate));
    processor.addControllerListener(this);

    ////////////////////////////////////////////////////////////
    // Construct the DataSink and employ an anonymous class as
    // a DataSink listener in order that the end of transfer
    // (completion of task) can be detected.
    ///////////////////////////////////////////////////////////
    source = processor.getDataOutput();
    try {
      sink = Manager.createDataSink(source, sinkLocation);
    } catch (Exception sinkException) {
      state = FAILED;
      processor.removeControllerListener(this);
      processor.close();
      processor = null;
      return;
    }
    sink.addDataSinkListener(new DataSinkListener() {
      public void dataSinkUpdate(DataSinkEvent e) {
        if (e instanceof EndOfStreamEvent) {
          sink.close();
          source.disconnect();
          if (state != FAILED)
            state = FINISHED;
        } else if (e instanceof DataSinkErrorEvent) {
          if (sink != null)
            sink.close();
          if (source != null)
            source.disconnect();
          state = FAILED;
        }
      }
    });
    // Start the transcoding
    processor.start();
  }

  /***************************************************************************
   * Alternate constructor: source and destination specified as Strings, and
   * no rate provided (hence rate of 1.0)
   **************************************************************************/
  Location2Location(String sourceName, String destinationName,
      Format[] outputFormats, ContentDescriptor outputContainer) {

    this(new MediaLocator(sourceName), new MediaLocator(destinationName),
        outputFormats, outputContainer);
  }

  /***************************************************************************
   * Alternate constructor: No rate specified therefore rate of 1.0
   **************************************************************************/
  Location2Location(MediaLocator sourceLocation,
      MediaLocator destinationLocation, Format[] outputFormats,
      ContentDescriptor outputContainer) {

    this(sourceLocation, destinationLocation, outputFormats,
        outputContainer, 1.0f);
  }

  /***************************************************************************
   * Alternate constructor: source and destination specified as Strings.
   **************************************************************************/
  Location2Location(String sourceName, String destinationName,
      Format[] outputFormats, ContentDescriptor outputContainer,
      double rate) {

    this(new MediaLocator(sourceName), new MediaLocator(destinationName),
        outputFormats, outputContainer, rate);
  }

  /***************************************************************************
   * Respond to events from the Processor performing the transcoding. If its
   * task is completed (end of media) close it down. If there is an error
   * close it down and mark the process as FAILED.
   **************************************************************************/
  public synchronized void controllerUpdate(ControllerEvent e) {

    if (state == FAILED)
      return;

    // Transcoding complete.
    if (e instanceof StopEvent) {
      processor.removeControllerListener(this);
      processor.close();
      if (state == TRANSLATING)
        state = TRANSFERRING;
    }
    // Transcoding failed.
    else if (e instanceof ControllerErrorEvent) {
      processor.removeControllerListener(this);
      processor.close();
      state = FAILED;
    }
  }

  /***************************************************************************
   * Initiate the transfer through a DataSink to the destination and wait
   * (block) until the process is complete (or failed) or the supplied number
   * of milliseconds timeout has passed. The method returns the total amount
   * of time it blocked.
   **************************************************************************/
  public int transfer(int timeOut) {

    // Can't initiate: Processor already failed to transcode
    ////////////////////////////////////////////////////////
    if (state == FAILED)
      return -1;

    // Start the DataSink
    //////////////////////
    try {
      sink.open();
      sink.start();
    } catch (Exception e) {
      state = FAILED;
      return -1;
    }
    if (state == TRANSLATING)
      state = TRANSFERRING;
    if (timeOut <= 0)
      return timeOut;

    // Wait till the process is complete, failed, or the
    // prescribed time has passed.
    /////////////////////////////////////////////////////
    int waited = 0;
    while (state != FAILED && state != FINISHED && waited < timeOut) {
      try {
        Thread.sleep(WAIT_PERIOD);
      } catch (InterruptedException ie) {
      }
      waited += WAIT_PERIOD;
    }
    return waited;
  }

  /***************************************************************************
   * Initiate the transfer through a DataSink to the destination but return
   * immediately to the caller.
   **************************************************************************/
  public void transfer() {

    transfer(-1);
  }

  /***************************************************************************
   * Determine the object's current state. Returns one of the class constants.
   **************************************************************************/
  public int getState() {

    return state;
  }

  /***************************************************************************
   * Returns the object's state as a String. A more user friendly version of
   * getState().
   **************************************************************************/
  public String getStateName() {

    return STATE_NAMES[state];
  }

  /***************************************************************************
   * Obtain the rate being used for the process. This is often 1, despite what
   * the user may have supplied as Clocks (hence Processors) don't have to
   * support any other rate than 1 (and will default to that).
   **************************************************************************/
  public float getRate() {

    return translationRate;
  }

  /***************************************************************************
   * Set the time at which media processing will stop. Specification is in
   * media time. This means only the first "when" amount of the media will be
   * transferred.
   **************************************************************************/
  public void setStopTime(Time when) {

    if (processor != null)
      processor.setStopTime(when);
  }

  /***************************************************************************
   * Stop the processing and hence transfer. This gives user control over the
   * duration of a transfer. It could be started with the transfer() call and
   * after a specified period stop() could be called.
   **************************************************************************/
  public void stop() {

    if (processor != null)
      processor.stop();
  }
}
