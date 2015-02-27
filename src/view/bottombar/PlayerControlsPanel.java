/*
 * This file is part of VLCJ.
 *
 * VLCJ is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * VLCJ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with VLCJ. If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2009, 2010, 2011, 2012, 2013, 2014 Caprica Software Limited.
 */
package view.bottombar;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import view.player.IMediaPlayer;
import controller.Globals;
public class PlayerControlsPanel extends JPanel {
    
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
   
	/**
	 * Single thread executor
	 */
	private final ScheduledExecutorService executorService = 
			Executors.newSingleThreadScheduledExecutor();
	
	private UpdateRunnable runnable;
    
	/**
	 * Time slider and label
	 */
	private JLabel timeLabel, remainingLabel;
    private JSlider positionSlider;
    
    /**
     * DOCUMENT ME 
     */
	@SuppressWarnings("unused")
	private boolean mousePressedPlaying = false;
	private boolean remaining = false;
    
    /**
     * Constructor method
     */
    public PlayerControlsPanel() {
        createUI();
    }
      
    /**
     * Constructor helper method
     */
    private void createUI() {
        createControls();
        layoutControls();
        registerListeners();
    }
    
    /**
     * Creates the time slider
     */
    private void createControls() {
        timeLabel = new JLabel("hh:mm:ss");
        remainingLabel = new JLabel("hh:mm:ss");
        positionSlider = new JSlider();
        positionSlider.setMinimum(0);
        positionSlider.setMaximum(10000);
        positionSlider.setValue(0);
        positionSlider.setToolTipText("Position");
    }
    
    /**
     * Creates the layoutControls
     */
    private void layoutControls() {
		setBorder(new EmptyBorder(4, 4, 4, 4));
		setLayout(new BorderLayout());
		JPanel positionPanel = new JPanel();
		positionPanel.setLayout(new GridLayout(1, 1));
		positionPanel.add(positionSlider);
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BorderLayout(8, 0));
		topPanel.add(timeLabel, BorderLayout.WEST);
		topPanel.add(remainingLabel, BorderLayout.EAST);
		topPanel.add(positionPanel, BorderLayout.CENTER);
		add(topPanel, BorderLayout.NORTH);
	}
    
    /**
     * Broken out position setting, handles updating mediaPlayer
     */
    private void setSliderBasedPosition() {
       float positionValue = positionSlider.getValue() / 10000.0f;
        // Avoid end of file freeze-up
        if (positionValue > 0.99f) {
            positionValue = 0.99f;
        }
        Globals.getVideoController().setPosition(positionValue);
        runnable.update();
    }
    

    private void registerListeners() {
        positionSlider.addMouseListener(new MouseAdapter() {@
            Override
            public void mousePressed(MouseEvent e) {
                if (Globals.getVideoController().isPlaying()) {
                    mousePressedPlaying = true;
                    Globals.getVideoController().play();
                } else {
                    mousePressedPlaying = false;
                }
                setSliderBasedPosition();
            }@
            Override
            public void mouseReleased(MouseEvent e) {
                setSliderBasedPosition();
            }
        });
        
        remainingLabel.addMouseListener(new MouseAdapter() {
        	public void mousePressed(MouseEvent e) {
        		remaining = !remaining;
        		runnable.update();
        	}
        });
    }
    
    private final class UpdateRunnable implements Runnable {
        private final IMediaPlayer mediaPlayer;
        private UpdateRunnable(IMediaPlayer mediaPlayer) {
            this.mediaPlayer = mediaPlayer;
        }@
        
        Override
        public void run() {
            final long time = mediaPlayer.getMediaTime();
            final long end = mediaPlayer.getMediaDuration();
            final int position = (int)(mediaPlayer.getPosition() * 10000.0f);
            
            // Updates to user interface components must be executed on 
            // the Event
            // Dispatch Thread
            SwingUtilities.invokeLater(new Runnable() {@
                Override
                public void run() {
                    if (mediaPlayer.isPlaying()) {
                        updateTime(time, end);
                        updatePosition(position);
                        controller.Controller.getInstance().updateLabels(time);
                    }
                }
            });
        }
        
        public void update()
        {
        	final long time = mediaPlayer.getMediaTime();
        	final long end = mediaPlayer.getMediaDuration();
        	updateTime(time, end);
        }
    }
    
    private void updateTime(long millis, long end) {
        // Playtime string
    	String s = String.format("%02d:%02d:%02d", 
        		TimeUnit.MILLISECONDS.toHours(millis), 
        		TimeUnit.MILLISECONDS.toMinutes(millis) - 
        		TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)), 
        		TimeUnit.MILLISECONDS.toSeconds(millis) - 
        		TimeUnit.MINUTES.toSeconds(
        				TimeUnit.MILLISECONDS.toMinutes(millis)
        		)        			
        	);
        
    	// RemainingTime string
    	if(remaining)
    		end = end - millis;
    	
        String r = String.format("%02d:%02d:%02d", 
        		TimeUnit.MILLISECONDS.toHours(end), 
        		TimeUnit.MILLISECONDS.toMinutes(end) - 
        		TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(end)), 
        		TimeUnit.MILLISECONDS.toSeconds(end) - 
        		TimeUnit.MINUTES.toSeconds(
        				TimeUnit.MILLISECONDS.toMinutes(end)
        		)        			
        	);
    	
    	// Set Times
        timeLabel.setText(s);
        remainingLabel.setText(r);
    }
    
    private void updatePosition(int value) {
        positionSlider.setValue(value);
    }
    
    public void playerStarted(IMediaPlayer player)
    {
    	long time = player.getMediaTime();
        long end = player.getMediaDuration();
        int position = (int)(player.getPosition() * 10000.0f);
        
        updateTime(time, end);
        updatePosition(position);
        
    	executorService.scheduleAtFixedRate(
    			runnable = new UpdateRunnable(player), 
    			0L, 
    			1L, 
    			TimeUnit.MILLISECONDS
    		);
    }
}