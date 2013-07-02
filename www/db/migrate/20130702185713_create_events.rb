class CreateEvents < ActiveRecord::Migration
  def change
  	create_table "events", :force => true do |t|
	    t.datetime "start",                                :null => false
	    t.integer  "duration",                             :null => false
	    t.datetime "end",                                  :null => false
	    t.integer  "activity_id"
	    t.integer  "min_duration",          :null => false
	    t.integer  "max_duration",          :null => false
	    t.integer  "tz_offset",             :default => 0, :null => false
	    t.string   "name",                  :null => false
	    t.text     "domain"
	    t.text     "ordered_events_before"

	    t.datetime "created_at", :null => false
	    t.datetime "updated_at", :null => false
	  end
  end
end
