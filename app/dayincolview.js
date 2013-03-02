// (c) 2013 Lukas Dolezal

// Vertical day view. 24 hours are on the vertical y axis and horizontaly is slidable days/weeks/months etc.
PersonalTimetabling.CalendarViews.VerticalDayView = PersonalTimetabling.AbstractTaskView.extend({

    options: {
        initial_date: Date.today()
    },

    initialize: function() {

        this.$el.addClass("dayincolview");
        
        this.$bar_container_window = $("<div id='headers-window'/>").appendTo(this.$el);
        this.$bar_container = $("<div id='mover-headers' />").appendTo(this.$bar_container_window);
        this.$headers_el = $("<div id='grid-headers' />").appendTo(this.$bar_container);
        this.$bar_container_window.append($("<div id='headers-corkiee'><div class='something'/></div>"));
        
        this.$container_window = $("<div id='mover-window'/>").appendTo(this.$el);
        this.$container = $("<div id='mover' />").appendTo(this.$container_window);
        this.$grid_el = $("<div id='grid' />").appendTo(this.$container);

        this.$hours_labels = $("<div id='grid-vertical'/>").appendTo(this.$container_window);
        this.render_hours_labels();
        
        this.$container.kinetic_draggable({axis: 'x', drag:_.bind(this.on_drag, this, this.$bar_container), stop: _.bind(this.on_drag, this, this.$bar_container)});
        this.$bar_container.kinetic_draggable({axis: 'x', drag:_.bind(this.on_drag, this, this.$container), stop: _.bind(this.on_drag, this, this.$container)});

        $(window).on("keypress", _.bind(function(e) {
            this.scroll(1);
        }, this));
        
        // clone zoom and geometry from class instance
        this.zoom = _.extend({}, this.zoom);
        this.geometry = _.extend({}, this.geometry);

        this.left_edge_date = this.options.initial_date;

        this.geometry.compute(this.zoom.pixel_minutes);
    },

    // current columns geometry configuration - abstracted from physical rendered units
    geometry: {

        // specifies length of column in real time minutes
        column_minutes: 60,

        // specifies rendered subcolumns axes
        subcolumns_count: 2,

        // specifies grouping "function" for supercolumn header
        // one of [HOUR, DAY, WEEK, MONTH, YEAR]
        supercolumn_groupby: 'DAY',

        // determine geometry by given minutes/pixel zoom
        compute: function(min_per_px) {
            var MIN_COL_WIDTH = 80;
            var GRANULARITY_LEVELS = [1440,2880,5760,10080];
            var SUPERCOLUM_LEVELS = ['WEEK','WEEK', 'MONTH', 'MONTH', 'MONTH']

            var selected = 0;
            while(selected < GRANULARITY_LEVELS.length && GRANULARITY_LEVELS[selected] / min_per_px < MIN_COL_WIDTH) {
                selected++;
            }

            this.column_minutes = GRANULARITY_LEVELS[selected];
            this.supercolumn_groupby = SUPERCOLUM_LEVELS[selected];
        },

        // return aligned date from given one according to current geometry setup.
        // ie when column_minutes is 30 it align date to multiply of 30 minutes
        // when cm is 2 - 24 hours it align to multiply starting from the midnight etc.
        align: function(date) {
            if (this.column_minutes <= 60) {
                // align to multiply of minutes
                return new Date(date.getTime()- (date.getTime() % (this.column_minutes*60000)));
            } else if (this.column_minutes <= 1440) {
                // align to multiply with period in midnight
                var midnight = date.getMidnight();
                var offset = date.diffMinutes(midnight);
                return midnight.addMinutes(offset - (offset % this.column_minutes));
            }
            return date;
        }
    },

    // current rendering zoom - translates abstracted columns to physical rendering units
    zoom: {
        // current minutes per one view pixel of width
        pixel_minutes: 6,

        // current count of columns are rendered (not the same as visible due to overflow hidding)
        rendering_columns_count: 0,

        // current rendering width.
        rendering_width: 0,

        // computes width of column from current zoom.pixel_minutes and geometry.column_minutes
        column_width: 0,

        // number of columns appended on each side for dragging seamless effect
        edge_columns: 4,

        // number of rendering minutes
        rendering_minutes: 0,

        recompute: function (timeline, set_pixel_minutes) {
            // compute column count to ensure whole rendering area is covered
            if (set_pixel_minutes != undefined) {
                this.pixel_minutes = Number(set_pixel_minutes);
            }

            var visible_width = timeline.$el.width();
            this.column_width = timeline.geometry.column_minutes / this.pixel_minutes;
            this.rendering_columns_count = Math.ceil(visible_width / this.column_width) + 2*this.edge_columns;
            this.rendering_width = this.column_width * this.rendering_columns_count;
            this.rendering_minutes = this.rendering_columns_count * timeline.geometry.column_minutes;
        },

    },

    // array of grid column elements
    $grid_cols: [],

    // date representing by first column in grid_cols array
    grid_cols_first_date: null,

    // date supposed to be on the left edge of screen
    left_edge_date: null,

    // array of supercolumn elements
    $grid_supercols: [],

    // redraws whole view
    render: function() {
        this.resize();
        this.render_columns();
        this.render_headers();
    },

    // refresh sizes of view
    resize: function() {
        var headers_height = this.$headers_el.innerHeight();
        this.$bar_container.height(headers_height);
        this.$container_window.height(this.$el.height()-headers_height);
        this.zoom.recompute(this);
    },

    // renders main grid columns
    render_columns: function() {
        this.$grid_el.empty();

        this.$grid_cols = [];

        // create number of visible columns plus 2 for left and right overflow
        for( var i = 0; i < this.zoom.rendering_columns_count; i++) {
            var gridcol = $("<div class='grid-col' />")
            .width(this.zoom.column_width)
            .css("left", this.zoom.column_width * i);
            
            this.$grid_cols.push({col: gridcol});
            this.$grid_el.append(gridcol);
        }

        this.grid_cols_first_date = this.geometry.align(this.left_edge_date).addMinutes(this.geometry.column_minutes * -this.zoom.edge_columns);
        this.$container.width(this.zoom.rendering_width);
        this.$bar_container.width(this.zoom.rendering_width);
        this.set_movers_offset(-this.left_edge_date.diffMinutes(this.grid_cols_first_date) / this.zoom.pixel_minutes);

    },

    // render headers
    render_headers: function() {
        this.$headers_el.empty();
        this.$grid_supercols = [];
        this.$grid_colsheaders = [];

        // compute maximum on screen visible headers
        var headers_count = 0;
        switch(this.geometry.supercolumn_groupby) {
            case 'HOUR':
                headers_count = Math.ceil(this.zoom.rendering_minutes / 60) + 1;
            case 'DAY':
                headers_count = Math.ceil(this.zoom.rendering_minutes / 1440) + 1;
                break;
            case 'MONTH':
                headers_count = Math.ceil(this.zoom.rendering_minutes / 40320) + 1;
                break;
            case 'WEEK':
                headers_count = Math.ceil(this.zoom.rendering_minutes / 10080) + 1;
                break;
            case 'YEAR':
                break;
            default:
                break;
        }

        // create supercolumns elements
        var supercols_header_container = $("<div class='header-supercols-container'/>").appendTo(this.$headers_el);
        
        for (var i = 0; i< headers_count; i++) {
            var label_el = $("<span class='header-col-label'></span>");
            var header_el = $("<div class='header-col'></div>").append(label_el);

            supercols_header_container.append(header_el);
            this.$grid_supercols.push({col: header_el, label: label_el});
        }

        // create number of visible columns
        var cols_header_container = $("<div class='header-cols-container'/>").appendTo(this.$headers_el);
        for( var i = 0; i < this.zoom.rendering_columns_count; i++) {
            var label_el = $("<div class='grid-header-col'/>")
            .width(this.zoom.column_width)
            .css("left", this.zoom.column_width * i);

            this.$grid_colsheaders.push({$el: label_el});
            cols_header_container.append(label_el);
        }


        this.update_headers();
    },

    render_hours_labels: function() {
        this.$hours_labels.empty();

        // add 24 hours boxes
        for(var i = 0; i< 24;i++) {
            $("<div class='hour-label' />")
            .text(i.pad(2) + ":00")
            .appendTo(this.$hours_labels);
        }
        
    },

    update_headers: function() {

        var left_date = this.grid_cols_first_date;
        var left_pixels = 0;
        
        for(var i = 0; i < this.$grid_supercols.length; i++) {
            // get date of end of current supercolumn determined by left date
            var next_stop = null;
            switch(this.geometry.supercolumn_groupby) {
                case 'HOUR':
                    next_stop = left_date.addMinutes(60);
                    break;
                case 'DAY':
                    // go to noon of next day and then get midnight to resolve +- 1 hour fluctuation in day length
                    // in DST
                    next_stop = left_date.getMidnight().addMinutes(770+1440).getMidnight();
                    break;
                case 'WEEK':
                    next_stop = left_date.getMidnight();
                    next_stop.setDate(next_stop.getDate() - left_date.getDayStarting(1) + 7);
                    break;
                case 'MONTH':
                    next_stop = new Date(left_date);
                    next_stop.setDate(1);
                    next_stop.setMonth(next_stop.getMonth() + 1);
                    next_stop.setHours(0,0,0);
                    break;
                default:
                    break;
            }

            var width = next_stop.diffMinutes(left_date) / this.zoom.pixel_minutes;
            this.$grid_supercols[i].col
                .css("left", left_pixels);
            this.$grid_supercols[i].label
                .text(this.header_label(left_date));

            if (Math.abs(this.$grid_supercols[i].col.width() - width) > 1) {
                this.$grid_supercols[i].col.width(width);
            }

            this.$grid_supercols[i].width = width;
            this.$grid_supercols[i].left = left_pixels;
            this.$grid_supercols[i].label_width =  this.$grid_supercols[i].label.innerWidth();

            left_date = next_stop;
            left_pixels += width;
        }

        // update cols headers
        for(var i = 0; i < this.$grid_colsheaders.length; i++) {
            var col_date = this.grid_cols_first_date.addMinutes(i * this.geometry.column_minutes);
            this.$grid_colsheaders[i].$el.text(this.column_label(col_date));
        }
        
        this.update_header_labels();
    },

    update_header_labels: function(container_offset) {
        if (container_offset == undefined) {
            container_offset = this.$container.offset();
        }
        
        for(var i = 0; i < this.$grid_supercols.length; i++) {
            // if left part of column is not visible, make label fixed
            if (this.$grid_supercols[i].left + container_offset.left < 60) {
                this.$grid_supercols[i].col.addClass("header-col-invisibleleft");
                this.$grid_supercol_affixed = i;
            }
            else {
                this.$grid_supercols[i].col.removeClass("header-col-invisibleleft");
            }

            if (this.$grid_supercols[i].left + container_offset.left < -this.$grid_supercols[i].width+this.$grid_supercols[i].label_width +60) {
                this.$grid_supercols[i].col.addClass("header-col-invisible");
            }
            else {
                this.$grid_supercols[i].col.removeClass("header-col-invisible");
            }
        }
        
    },


    // get label on column according current zoom
    column_label: function(col_date) {
        if(this.geometry.column_minutes < 1440) {
                return col_date.getHours().pad(2) + ":" + col_date.getMinutes().pad(2);
        } else if (this.geometry.column_minutes < 10080) {
                return col_date.getDate() + ". " + (col_date.getMonth()+1) + ". " + col_date.getFullYear();
        } else if (this.geometry.column_minutes < 40320) {
                return "Week" + col_date.getWeekOfYear();
        } else {
            // month
            return (col_date.getMonth() + 1) + "/" + col_date.getFullYear();
        }

    },

    header_label: function(header_date) {
        switch(this.geometry.supercolumn_groupby) {
            case 'HOUR':
                return header_date.getHours().pad(2) + ":" + header_date.getMinutes().pad(2);
            case 'DAY':
                return header_date.getDate() + ". " + (header_date.getMonth()+1) + ". " + header_date.getFullYear();
            case 'WEEK':
                return "Week " + header_date.getWeekOfYear() + "/" + header_date.getFullYear();
            case 'MONTH':
                return (header_date.getMonth() + 1) + "/" + header_date.getFullYear();
            default:
                return "";
        }
    },

    shiftLeft: function(colsnumber) {
        // set new first col date
        this.grid_cols_first_date = this.grid_cols_first_date.addMinutes(colsnumber * this.geometry.column_minutes);
        this.update_headers();
    },

    // scroll view to right - ie prepend days to left
    shiftRight: function(colsnumber) {
        // set new first col date
        this.grid_cols_first_date = this.grid_cols_first_date.addMinutes(colsnumber * -this.geometry.column_minutes);
        this.update_headers();
    },


    scroll: function(pixels) {
        var container_offset = this.$container.offset();
        this.$container.offset({left: container_offset.left + pixels});
        this.$bar_container.offset({left: container_offset.left + pixels});

        this.scroll_masquerade(this.$container.offset());
    },

    set_movers_offset: function(left){
        this.$container.css("left", left);
        this.$bar_container.css("left", left);
    },
    
    scroll_masquerade: function(current_offset) {

        var grid_left = current_offset.left;

        if (grid_left > 0) {

            // number of columns to prerender and shift
            var number = Math.ceil(grid_left / (this.zoom.column_width * this.zoom.edge_columns))
                * this.zoom.edge_columns;

            this.shiftRight(number);

            // move to seamless position
            this.set_movers_offset( (-this.zoom.edge_columns * this.zoom.column_width)
                + (grid_left % (this.zoom.edge_columns * this.zoom.column_width)));

            this.left_edge_date = this.grid_cols_first_date.addMinutes(-this.$container.offset().left * this.zoom.pixel_minutes);
            return true;
        } else if (grid_left < -2*this.zoom.edge_columns*this.zoom.column_width ) {

            // how many pixels is missing in the right
            var overflow = -(grid_left + this.zoom.column_width*2*this.zoom.edge_columns);

            // number of columns to prerender and shift
            var number = Math.ceil(Math.ceil(overflow / this.zoom.column_width) / this.zoom.edge_columns)
                * this.zoom.edge_columns;

            this.shiftLeft(number);

            // move to seamless position
            this.set_movers_offset( (-this.zoom.edge_columns * this.zoom.column_width)
            - (overflow % (this.zoom.edge_columns * this.zoom.column_width)));

            this.left_edge_date = this.grid_cols_first_date.addMinutes(-this.$container.offset().left * this.zoom.pixel_minutes);
            return true;
        } else {
            // update headers text alginments
            this.update_header_labels(current_offset);
        }

        if (this.$grid_supercol_affixed !== undefined) {
            this.$grid_supercols[this.$grid_supercol_affixed].label.css("left", -current_offset.left - this.$grid_supercols[this.$grid_supercol_affixed].left);
        }
        
        this.left_edge_date = this.grid_cols_first_date.addMinutes(-current_offset.left * this.zoom.pixel_minutes);
        return false;
        
    },

    on_drag: function(other, e, ui) {
        // drag also other elements
        other.offset({left: ui.position.left});
        
        if (this.scroll_masquerade(ui.position)) {
            // reset mouse position in drag
            if (typeof ui.helper !== "undefined" && typeof ui.helper.kinetic_draggable === "function") {
                ui.helper.kinetic_draggable("resetMouse", e);
            }
        }

    },

    setZoom: function(z) {

        //console.log("left edge is on ", this.left_edge_date);

        // change pixel minutes
        this.zoom.pixel_minutes = z / 10;

        // change geometry to reflect it
        this.geometry.compute(this.zoom.pixel_minutes);

        this.render();
    },

});