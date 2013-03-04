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

        this.geometry.compute(this.zoom.pixel_days);
    },

    // current columns geometry configuration - abstracted from physical rendered units
    geometry: {

        // specifies length of column in units. doesnot explicitly means constant time ammount
        column_units: 1,

        column_unit: 'DAY',

        // specifies rendered subcolumns axes
        subcolumns_count: 4,

        // specifies grouping "function" for supercolumn header
        // one of [HOUR, DAY, WEEK, MONTH, YEAR]
        supercolumn_groupby: 'WEEK',

        // determine geometry by given minutes/pixel zoom
        compute: function(days_per_pixel) {
            var MIN_COL_WIDTH = 50;
            var GRANULARITY_LEVELS = [1,2,1,2,1];
            var GRANULARITY_UNITS = ['DAY', 'DAY', 'WEEK','WEEK','MONTH'];
            var SUPERCOLUM_LEVELS = ['WEEK','WEEK','MONTH','MONTH', 'YEAR'];

            var selected = 0;
            while(selected < GRANULARITY_LEVELS.length &&
                (GRANULARITY_LEVELS[selected]*this.unit_minutes_limit(GRANULARITY_UNITS[selected]) / 1500) / days_per_pixel < MIN_COL_WIDTH) {
                selected++;
            }

            if (selected >= GRANULARITY_LEVELS.length)
                selected--;

            this.column_units = GRANULARITY_LEVELS[selected];
            this.column_unit = GRANULARITY_UNITS[selected];
            this.supercolumn_groupby = SUPERCOLUM_LEVELS[selected];
        },

        // align given date to the nearest earlier eadge of larger unit
        align: function(date, unit) {
            if (unit == undefined)
                unit = this.column_unit;
            
            switch(unit) {
                case 'HOUR':
                    return new Date(date.getFullYear(), date.getMonth(), date.getDate, date.getHours(), 0,0);
                    break;
                case 'DAY':
                    // go to noon of next day and then get midnight to resolve +- 1 hour fluctuation in day length
                    // in DST
                    return date.getMidnight();
                    break;
                case 'WEEK':
                    return date.getMidnight().addDays(- date.getDayStarting(1));
                    break;
                case 'MONTH':
                    next_stop = new Date(date);
                    next_stop.setDate(1);
                    next_stop.setHours(0,0,0);
                    return next_stop;
                    break;
                case 'YEAR':
                    return new Date(date.getFullYear(), 0,0,0,0,0);
                default:
                    return date;
                    break;
            }
        },

        // get the nearest latter edge of larger time unit
        next: function(date, unit){
            if (unit === undefined)
                unit = this.column_unit;

            switch(unit) {
                case 'HOUR':
                    return new Date(date.getFullYear(), date.getMonth(), date.getDate, date.getHours() + 1, 0,0);
                    break;
                case 'DAY':
                    // go to noon of next day and then get midnight to resolve +- 1 hour fluctuation in day length
                    // in DST
                    return date.getMidnight().addDays(1);
                    break;
                case 'WEEK':
                    return date.getMidnight().addDays(- date.getDayStarting(1) + 7);
                    break;
                case 'MONTH':
                    next_stop = new Date(date);
                    next_stop.setDate(1);
                    next_stop.setMonth(next_stop.getMonth() + 1);
                    next_stop.setHours(0,0,0);
                    return next_stop;
                    break;
                case 'YEAR':
                    return new Date(date.getFullYear() + 1, 0,0,0,0,0);
                default:
                    break;
            }            
        },

        unit_minutes_limit: function(unit) {
            switch(unit) {
                case 'HOUR':
                    return 60;
                    break;
                case 'DAY':
                    // go to noon of next day and then get midnight to resolve +- 1 hour fluctuation in day length
                    // in DST
                    // 25 hours a day
                    return 1500;
                    break;
                case 'WEEK':
                    // better bound is only 1 day in week can have 25 hours
                    // but it is for compatibility with cross unit computations (ie WEEK / DAY returns 7 not 6,76)
                    return 7*1500;
                    break;
                case 'MONTH':
                    // similar to week
                    return 31*1500;
                    break;
                case 'YEAR':
                    return 31*12*1500;
                default:
                    break;
            }

        },
        
        upper_limit_column_timeunits: function(unit) {
            var column_unit_max_minutes = this.unit_minutes_limit(this.column_unit);
            return this.column_units * column_unit_max_minutes / this.unit_minutes_limit(unit);
        },

        get_column_next: function(date) {
            var column_date = date;
            var i = this.column_units;
            while(i-- > 0) {column_date = this.next(column_date, this.column_unit);}
            return column_date;
        },
        
        // returns count of units inside column representing given time period
        get_column_units: function(date, unit) {
            var column_start = this.align(date, this.column_unit);
            // find next column

            return this.get_column_next(date).diff(unit, column_start);
        }
    },

    // current rendering zoom - translates abstracted columns to physical rendering units
    zoom: {
        // current days per pixel. day not neccessary mean 24 hours
        pixel_days: 1/80,

        // current count of columns are rendered (not the same as visible due to overflow hidding)
        rendering_columns_count: 0,

        // current rendering width.
        rendering_width: 0,

        max_column_width: 0,
        
        // number of columns appended on each side for dragging seamless effect
        edge_columns: 4,

        // offset at which right edge is visible in view
        right_edge_offset: 0,

        // maximal width of rendering area
        max_rendering_width: 0,
        
        recompute: function (timeline) {

            var visible_width = timeline.$el.width();
            this.max_column_width = timeline.geometry.upper_limit_column_timeunits('DAY') / this.pixel_days;
            this.rendering_columns_count = Math.ceil(visible_width / this.max_column_width) + 2*this.edge_columns;
            this.max_rendering_width = this.max_column_width * this.rendering_columns_count;

            console.log('zoom recomputed: ', this);
        },

        column_width: function(geometry, date) {
            var column_days = geometry.get_column_units(date, 'DAY');
            return column_days / this.pixel_days;
        }
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
        this.redraw();
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
            var gridcol = $("<div class='grid-col' />");
            
            this.$grid_cols.push({col: gridcol});
            this.$grid_el.append(gridcol);
        }
    },

    // render headers
    render_headers: function() {
        this.$headers_el.empty();
        this.$grid_supercols = [];
        this.$grid_colsheaders = [];

        // compute maximum on screen visible headers
        // ie max supercol units in visible days pixels
        var headers_count = this.zoom.max_rendering_width * this.zoom.pixel_days * this.geometry.unit_minutes_limit('DAY') / this.geometry.unit_minutes_limit(this.geometry.supercolumn_groupby);

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
            var label_el = $("<div class='grid-header-col'/>");
            
            this.$grid_colsheaders.push({$el: label_el});
            cols_header_container.append(label_el);
        }
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

    // UPDATE COLUMNS WIDTH AND ALIGNMENT
    update_columns_rendering: function() {
        // align first column to left edge minus edge columns
        this.grid_cols_first_date = this.left_edge_date.addMinutes(1);
        var c = this.zoom.edge_columns;
        while(c-- > 0) {this.grid_cols_first_date = this.geometry.align(this.grid_cols_first_date.addMinutes(-1));}

        var left_date = this.grid_cols_first_date;
        var left_pixels = 0;
        
        for(var i = 0; i< this.$grid_cols.length; i++) {
            // column width is variable (ie column of month)
            var width = this.zoom.column_width(this.geometry,left_date);

            this.$grid_cols[i].width = width;
            this.$grid_cols[i].left = left_pixels;
            this.$grid_cols[i].date = left_date;
            this.$grid_cols[i].col.width(width);
            this.$grid_cols[i].col.css("left", left_pixels);

            left_pixels += width;
            left_date = this.geometry.get_column_next(left_date);
        }

        this.zoom.rendering_width = left_pixels;
        this.$container.width(this.zoom.rendering_width);
        this.$bar_container.width(this.zoom.rendering_width);
        this.zoom.right_edge_offset = - (this.zoom.rendering_width - this.$el.width());
    },

    update_headers: function() {
        // compute start of first supercolumn.
        var left_date = this.geometry.align(this.grid_cols_first_date, this.geometry.supercolumn_groupby);
        var left_pixels = -this.grid_cols_first_date.diff('DAY',left_date) / this.zoom.pixel_days;
        
        for(var i = 0; i < this.$grid_supercols.length; i++) {
            // get date of end of current supercolumn determined by left date
            var next_stop = this.geometry.next(left_date, this.geometry.supercolumn_groupby);

            var width = next_stop.diff('DAY',left_date) / this.zoom.pixel_days;
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
            this.$grid_colsheaders[i].$el
                .width(this.$grid_cols[i].width)
                .css("left", this.$grid_cols[i].left)
                .text(this.column_label(this.$grid_cols[i].date));
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
        if(this.geometry.column_unit == 'HOUR') {
                return col_date.getHours().pad(2) + ":" + col_date.getMinutes().pad(2);
        } else if (this.geometry.column_unit == 'DAY') {
                return col_date.getDate() + ". " + (col_date.getMonth()+1) + ". " + col_date.getFullYear();
        } else if (this.geometry.column_unit == 'WEEK') {
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
            case 'YEAR':
                return header_date.getFullYear();
            default:
                return "";
        }
    },

    redraw: function() {
        // set new first col date
        this.update_columns_rendering();
        this.update_headers();
        this.set_movers_offset(this.grid_cols_first_date.diff('DAY',this.left_edge_date) / this.zoom.pixel_days);
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

        this.left_edge_date = this.grid_cols_first_date.addMinutes(-current_offset.left * this.zoom.pixel_days * 1440);
        
        if (grid_left > 0) {
            this.redraw();
            return true;
        } else if (grid_left <  this.zoom.right_edge_offset) {
            this.redraw();
            return true;
        } else {
            // update headers text alginments
            this.update_header_labels(current_offset);
        }

        if (this.$grid_supercol_affixed !== undefined) {
            this.$grid_supercols[this.$grid_supercol_affixed].label.css("left", -current_offset.left - this.$grid_supercols[this.$grid_supercol_affixed].left);
        }
        
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
        this.zoom.pixel_days = Math.max(0.01,z/10000);

        // change geometry to reflect it
        this.geometry.compute(this.zoom.pixel_days);

        this.render();
    },

});