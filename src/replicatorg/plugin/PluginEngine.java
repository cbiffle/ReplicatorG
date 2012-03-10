package replicatorg.plugin;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import replicatorg.app.gcode.GCodeCommand;
import replicatorg.model.GCodeSource;

public class PluginEngine implements GCodeSource {
	GCodeSource parent = null;
	
	void setParentSource(GCodeSource parent) {
		this.parent = parent;
	}
	
	Vector<PluginEntry> plugins;
	void setPlugins(Vector<PluginEntry> plugins) {
		this.plugins = plugins;
	}
	
	@Override public int getLineCount() {
		return parent.getLineCount();
	}

	private void processLine(String line) {
		GCodeCommand mcode = GCodeCommand.parse(line);
		if( mcode.hasCode('M')) {
			double code = mcode.getCodeValue('M');
		
			for (PluginEntry plugin : plugins) {
				if (plugin instanceof MCodePlugin) {
					MCodePlugin mcp = (MCodePlugin)plugin;
					int codes[] = mcp.getAcceptedMCodes();
					for (int acceptedCode : codes) {
						if (code == acceptedCode) {
							mcp.processMCode(mcode);
						}
					}
				}
			}
		}
	}
	
	class GCodeIterator implements Iterator<String> {
		private Iterator<String> parent;
		public GCodeIterator(Iterator<String> parent) {
			this.parent = parent;
		}
		@Override public boolean hasNext() {
			return parent.hasNext();
		}

		@Override public String next() {
			String next = parent.next();
			processLine(next);
			return next;
		}

		@Override public void remove() {
			parent.remove();
		}
	}
	
	@Override public Iterator<String> iterator() {
		return new GCodeIterator(parent.iterator());
	}

	@Override
	public List<String> asList() {
		List<String> result = new ArrayList<String>();
		for(Iterator<String> i = iterator(); i.hasNext();)
			result.add(i.next());
		return result;
	}

}
