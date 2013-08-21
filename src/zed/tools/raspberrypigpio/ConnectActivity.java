package zed.tools.raspberrypigpio;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import zed.tools.raspberrypigpio.R;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class ConnectActivity extends Activity
{
    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_connect );

        m_ipAddressTxt = (EditText) findViewById( R.id.ip_address );
        m_portTxt = (EditText) findViewById( R.id.port );
        m_connectBtn = (Button) findViewById( R.id.connect );
        m_sendTxt = (EditText) findViewById( R.id.text_to_send );
        m_sendBtn = (Button) findViewById( R.id.send );
        m_receiveTxt = (EditText) findViewById( R.id.log_view );

        // Button press event listener.
        m_connectBtn.setOnClickListener( new View.OnClickListener()
        {
            public void onClick( View v )
            {
                String ipStr = m_ipAddressTxt.getText().toString();
                int port = Integer.parseInt( m_portTxt.getText().toString() );

                try
                {
                    m_client = new Socket( ipStr, port );

                    if ( m_client.isConnected() )
                    {
                        m_sendTxt.setEnabled( true );
                        m_connectBtn.setEnabled( false );
                    }
                }
                catch ( UnknownHostException e )
                {
                    e.printStackTrace();
                }
                catch ( IOException e )
                {
                    e.printStackTrace();
                }
            }
        } );

        // Button press event listener.
        m_sendBtn.setOnClickListener( new View.OnClickListener()
        {
            public void onClick( View v )
            {
                String sendStr = m_sendTxt.getText().toString();

                try
                {
                    PrintWriter printWriter = new PrintWriter( m_client.getOutputStream(), true );
                    printWriter.write( sendStr );
                    printWriter.flush();
                    printWriter.close();
                }
                catch ( UnknownHostException e )
                {
                    e.printStackTrace();
                }
                catch ( IOException e )
                {
                    e.printStackTrace();
                }
            }
        } );
    }

    private void setConnected( boolean isConnected )
    {
        if ( isConnected )
        {
            m_sendTxt.setEnabled( true );
            m_connectBtn.setEnabled( false );
        }
        else
        {
            m_sendTxt.setEnabled( false );
            m_connectBtn.setEnabled( true );

            try
            {
                m_client.close();
            }
            catch ( UnknownHostException e )
            {
                e.printStackTrace();
            }
            catch ( IOException e )
            {
                e.printStackTrace();
            }
        }
    }

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
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate( R.menu.connect, menu );

        return true;
    }

    private EditText m_ipAddressTxt;
    private EditText m_portTxt;
    private EditText m_sendTxt;
    private EditText m_receiveTxt;
    private Button   m_connectBtn;
    private Button   m_sendBtn;
    private Socket   m_client;
}
