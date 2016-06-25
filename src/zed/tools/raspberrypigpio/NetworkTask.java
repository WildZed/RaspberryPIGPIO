package zed.tools.raspberrypigpio;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import android.os.AsyncTask;
import android.util.Log;


public class NetworkTask extends AsyncTask<Void, byte[], Boolean>
{
    public NetworkTask()
    {
        m_ipStr = "192.168.1.1";
        m_port = 3142;
    }


    public NetworkTask( String ipStr, int port, NetworkTaskCallbacks callbacks )
    {
        m_ipStr = ipStr;
        m_port = port;
        m_callbacks = callbacks;
    }


    protected void finalize()
    {
        try
        {
            m_socket.close();
        }
        catch ( Exception e ) {}
    }


    public Boolean isConnected()
    {
        return null != m_socket && m_socket.isConnected();
    }


    public void sendDataToNetwork( String data )
    {
        // You run this from the main thread.
        try
        {
            if ( null != m_socket && m_socket.isConnected() )
            {
                Log.i( "AsyncTask", "SendDataToNetwork: Writing received message to socket" );
                m_outputStream.write( data.getBytes() );
                // PrintWriter printWriter = new PrintWriter( m_client.getOutputStream(), true );
                // printWriter.write( sendStr );
                // printWriter.flush();
                // printWriter.close();
            }
            else
            {
                Log.i( "AsyncTask", "SendDataToNetwork: Cannot send message. Socket is closed" );
            }
        }
        catch ( Exception e )
        {
            Log.i( "AsyncTask", "SendDataToNetwork: Message send failed. Caught an exception" );
        }
    }


    @Override
    protected Boolean doInBackground( Void... params )
    {
        // This runs on a different thread.
        boolean result = false;

        try
        {
            Log.i( "AsyncTask", "doInBackground: Creating socket" );

            SocketAddress sockaddr = new InetSocketAddress( m_ipStr, m_port );

            m_socket = new Socket();
            m_socket.connect( sockaddr, 2000 ); // 2 second connection timeout.

            if ( m_socket.isConnected() )
            {
                m_inputStream = m_socket.getInputStream();
                m_outputStream = m_socket.getOutputStream();

                // Tell the main thread we're connected.
                publishProgress( new byte[0] );

                Log.i( "AsyncTask", "doInBackground: Socket created, streams assigned" );
                Log.i( "AsyncTask", "doInBackground: Waiting for inital data..." );

                byte[] buffer = new byte[4096];
                int read;

                while ( -1 != ( read = m_inputStream.read( buffer, 0, 4096 ) ) )
                {
                    byte[] data = new byte[read];
                    System.arraycopy( buffer, 0, data, 0, read );
                    // Send the data back to the main thread.
                    publishProgress( data );
                    Log.i( "AsyncTask", "doInBackground: Got some data" );
                }
            }
        }
        catch ( IOException e )
        {
            m_exception = e;
            e.printStackTrace();
            Log.i( "AsyncTask", "doInBackground: IOException" );
            result = true;
        }
        catch ( Exception e )
        {
            m_exception = e;
            e.printStackTrace();
            Log.i( "AsyncTask", "doInBackground: Exception" );
            result = true;
        }
        finally
        {
            try
            {
                if ( null != m_inputStream )
                {
                    m_inputStream.close();
                    m_inputStream = null;
                }

                if ( null != m_inputStream )
                {
                    m_outputStream.close();
                    m_outputStream = null;
                }

                m_socket.close();
                m_socket = null;
            }
            catch ( IOException e )
            {
                m_exception = e;
                e.printStackTrace();
            }
            catch ( Exception e )
            {
                m_exception = e;
                e.printStackTrace();
            }

            Log.i( "AsyncTask", "doInBackground: Finished" );
        }

        return result;
    }


    @Override
    protected void onPreExecute()
    {
        Log.i( "AsyncTask", "onPreExecute" );
    }


    @Override
    protected void onProgressUpdate( byte[]... values )
    {
        if ( values.length > 0 )
        {
            if ( values[0].length > 0 )
            {
                Log.i( "AsyncTask", "onProgressUpdate: " + values[0].length + " bytes received." );

                onReceiveInput( new String( values[0] ) );
            }
            else
            {
                onConnect();
            }
        }
    }


    @Override
    protected void onCancelled()
    {
        Log.i( "AsyncTask", "Cancelled." );

        onDisconnect();
    }


    @Override
    protected void onPostExecute( Boolean result )
    {
        if ( result )
        {
            Log.i( "AsyncTask", "onPostExecute: Completed with an Error." );
            onReceiveInput( "There was a connection error:\n" + m_exception.getMessage() + "\n" );
        }
        else
        {
            Log.i( "AsyncTask", "onPostExecute: Completed." );
        }

        onDisconnect();
    }


    private void onReceiveInput( String data )
    {
        if ( null != m_callbacks )
        {
            m_callbacks.onReceiveInput( data );
        }
    }


    private void onConnect()
    {
        if ( null != m_callbacks )
        {
            m_callbacks.onConnect();
        }
    }


    private void onDisconnect()
    {
        if ( null != m_callbacks )
        {
            m_callbacks.onDisconnect();
        }
    }


    private String               m_ipStr;       // IP as string.
    private int                  m_port;        // Port number.
    private NetworkTaskCallbacks m_callbacks;   // The callbacks interface.
    private Socket               m_socket;      // Network socket.
    private InputStream          m_inputStream; // Network input stream.
    private OutputStream         m_outputStream; // Network output stream.
    private Exception            m_exception;   // An exception.
}
