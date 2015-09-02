package template_configurator;

import org.simpleframework.xml.*;

@Root
public class property {
	@Element
	String name;
	@Element
	String render_type;


	public String getRender_type() {
		return render_type;
	}

	public void setRender_type(String render_type) {
		this.render_type = render_type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public property(String name, String render_type) {
		super();
		this.name = name;
		this.render_type = render_type;
	}
	
	
}
