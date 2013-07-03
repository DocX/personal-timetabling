class CreateActivities < ActiveRecord::Migration
  def change
  	create_table "activities", :force => true do |t|
	    t.string   "name",       :null => false
	    t.text     "definition", :null => true
	    t.boolean  "link_events", :default => false, :null =>false
	    t.string   "link_comparator", :null => true
	    t.datetime "created_at", :null => false
	    t.datetime "updated_at", :null => false
	  end
  end
end
