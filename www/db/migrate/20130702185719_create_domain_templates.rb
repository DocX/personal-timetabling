class CreateDomainTemplates < ActiveRecord::Migration
  def change
  	create_table "domain_templates", :force => true do |t|
	    t.string  "name"
	    t.text    "domain"
	    t.integer "reference_count", :default => 0, :null => false
	  end
  end
end
