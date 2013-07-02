class CreateActivities < ActiveRecord::Migration
  def change
  	create_table "activities", :force => true do |t|
	    t.string   "name",       :null => false
	    t.text     "definition", :null => true
	    t.datetime "created_at", :null => false
	    t.datetime "updated_at", :null => false
	  end
  end
end
