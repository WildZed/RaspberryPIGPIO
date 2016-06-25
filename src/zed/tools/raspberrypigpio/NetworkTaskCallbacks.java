package zed.tools.raspberrypigpio;

public interface NetworkTaskCallbacks
{
    public void onReceiveInput( String data );


    public void onConnect();


    public void onDisconnect();
}
