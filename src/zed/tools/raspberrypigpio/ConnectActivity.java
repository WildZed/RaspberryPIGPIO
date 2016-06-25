package zed.tools.raspberrypigpio;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import zed.tools.raspberrypigpio.R;
import zed.tools.raspberrypigpio.NetworkTask;


public class ConnectActivity extends Activity implements NetworkTaskCallbacks
{
    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_connect );

        m_mainLayout = (RelativeLayout) findViewById( R.id.mainLayout );
        m_inputMethodManager = (InputMethodManager) getSystemService( Context.INPUT_METHOD_SERVICE );
        m_resources = getResources();
        m_ipAddressTxt = (EditText) findViewById( R.id.ip_address );
        m_portTxt = (EditText) findViewById( R.id.port );
        m_connectBtn = (Button) findViewById( R.id.connect );
        m_sendTxt = (EditText) findViewById( R.id.text_to_send );
        m_sendBtn = (Button) findViewById( R.id.send );
        m_receiveTxt = (EditText) findViewById( R.id.log_view );

        m_mainLayout.requestFocus();

        loadPreferences();

        // Button press event listener.
        m_connectBtn.setOnClickListener( m_connectBtnListener );

        // Button press event listener.
        m_sendBtn.setOnClickListener( m_sendBtnListener );
    }


    private void loadPreferences()
    {
        SharedPreferences settings = getPreferences( Context.MODE_PRIVATE );
        String ipAddress = settings.getString( "ip", m_resources.getText( R.string.ip_default ).toString() );
        String port = settings.getString( "port", m_resources.getText( R.string.port_default ).toString() );
        String sendTxt = settings.getString( "sendText", "" );

        m_ipAddressTxt.setText( ipAddress );
        m_portTxt.setText( port );
        m_sendTxt.setText( sendTxt );
    }


    private void savePreferences()
    {
        SharedPreferences settings = getPreferences( Context.MODE_PRIVATE );
        SharedPreferences.Editor editor = settings.edit();

        editor.putString( "ip", m_ipAddressTxt.getText().toString() );
        editor.putString( "port", m_portTxt.getText().toString() );
        editor.putString( "sendText", m_sendTxt.getText().toString() );

        // Commit the edits!
        editor.commit();
    }


    private void loseKeyboard()
    {
        m_inputMethodManager.hideSoftInputFromWindow( m_mainLayout.getWindowToken(), 0 );
    }


    private void connect()
    {
        if ( null != m_networkTask )
        {
            m_networkTask.cancel( true );
            m_networkTask = null;
        }

        logLabel( "Connecting...", false );

        String ipStr = m_ipAddressTxt.getText().toString();
        int port = Integer.parseInt( m_portTxt.getText().toString() );

        m_networkTask = new NetworkTask( ipStr, port, this );
        m_networkTask.execute();
    }


    private void setConnected( boolean isConnected )
    {
        if ( isConnected )
        {
            // m_connectBtn.setEnabled( false );
            m_sendBtn.setEnabled( true );
            logLabel( "Connected.", true );
        }
        else
        {
            // m_connectBtn.setEnabled( true );
            m_sendBtn.setEnabled( false );
            logLabel( "Disconnected.", true );

            if ( null != m_networkTask )
            {
                m_networkTask.cancel( true );
                m_networkTask = null;
            }
        }
    }


    private void sendMessage()
    {
        String sendStr = m_sendTxt.getText().toString();
        int selStart = m_sendTxt.getSelectionStart();
        int selEnd = m_sendTxt.getSelectionEnd();

        if ( selStart != selEnd )
        {
            sendStr = sendStr.substring( selStart, selEnd );
        }

        logMessage( "Sending message:", sendStr );

        m_networkTask.sendDataToNetwork( sendStr );
    }


    private void logLabel( String label, boolean append )
    {
        if ( append )
        {
            m_receiveTxt.append( "> " + label + "\n" );
        }
        else
        {
            m_receiveTxt.setText( "> " + label + "\n" );
        }
    }


    private void logMessage( String label, String message )
    {
        m_receiveTxt.append( "> " + label + "\n" );
        m_receiveTxt.append( message );

        if ( !message.endsWith( "\n" ) )
        {
            m_receiveTxt.append( "\n" );
        }
    }


    // Connect button listener.
    private View.OnClickListener m_connectBtnListener = new View.OnClickListener()
                                                      {
                                                          public void onClick( View v )
                                                          {
                                                              loseKeyboard();
                                                              connect();
                                                          }
                                                      };

    // Send button listener.
    private View.OnClickListener m_sendBtnListener    = new View.OnClickListener()
                                                      {
                                                          public void onClick( View v )
                                                          {
                                                              loseKeyboard();
                                                              sendMessage();
                                                          }
                                                      };


    @Override
    protected void onStart()
    {
        super.onStart();
    }


    @Override
    protected void onStop()
    {
        super.onStop();
        setConnected( false );
        savePreferences();
    }


    @Override
    protected void onPause()
    {
        super.onPause();
    }


    @Override
    protected void onRestart()
    {
        super.onRestart();
    }


    @Override
    protected void onResume()
    {
        super.onResume();
    }


    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        setConnected( false );
    }


    @Override
    protected void onSaveInstanceState( Bundle outState )
    {
        super.onSaveInstanceState( outState );
    }


    @Override
    public boolean onCreateOptionsMenu( Menu menu )
    {
        super.onCreateOptionsMenu( menu );

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate( R.menu.connect, menu );

        return true;
    }


    public void onConnect()
    {
        setConnected( true );
    }


    public void onReceiveInput( String data )
    {
        logMessage( "Received message:", data );
    }


    public void onDisconnect()
    {
        setConnected( false );
    }


    private RelativeLayout     m_mainLayout;
    private InputMethodManager m_inputMethodManager;
    private Resources          m_resources;
    private EditText           m_ipAddressTxt;
    private EditText           m_portTxt;
    private EditText           m_sendTxt;
    private EditText           m_receiveTxt;
    private Button             m_connectBtn;
    private Button             m_sendBtn;
    private NetworkTask        m_networkTask;
}
