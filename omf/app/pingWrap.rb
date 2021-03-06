#!/usr/bin/ruby

require 'rubygems'
require 'oml4r'

class MPStat < OML4R:MPBase
	name :myping
	param :dest_addr, :type => :string
	param :ttl, :type => :int32
	param :rtt, :type => :double
	param :rtt_unit, :type => :string
end # MPStat

class Wrapper
	def initialize(args)
		
		@addr  = nil
		@count = 0
		@interval = 1

		OML4R::init(args, :appName => 'pingmonitor') do |argParser|
			argParser.banner = "\nExecute  a wrapper around ping\n Use -h  or --help for a list of options\n\n"
			argParser.on("-a", "--dest_addr ADDRESS", "Address to ping") { |address| @addr = address.to_s }
			argParser.on("-c", "--count NUMBER", "Number of pings (default: infinite)") { |count| @count = count.to_i }
			argParser.on("-i", "--interval NUMBER", "Interval between pings (seconds)") { |interval| @interval = interval.to_i }
		end

		if @addr.nil? # Why was this unless @addr != nil ???
			raise "You did not specify an address to ping! (-a option)"
		end
	end # initialize

	def process_output(output)
		lines = output.split("\n")
		row = lines[1]
		column = row.split(" ")
		column[3].delete!(":")
		seq = column[4].split('=')
		ttl = column[5].split('=')
		rtt = column[6].split('=')

		puts row

		MPStat.inject(column[3], ttl[1], rtt[1], column[7]) # We only use certain portions, why not keep them in variables?
	end # process_output

	def ping
		output  = `/bin/ping -n -c 1 #{@addr}`
		process_output(output)
		sleep @interval
	end # ping

	def start
		if @count != 0
			(1..@count).each {
				ping
			}
		else
			loop { ping }
		end
	end # start
end # Wrapper

begin
	app = Wrapper.new(ARGV)
	app.start
rescue Exception => ex
	puts "Received an Exception when executing the ping wrapper."
	puts "Ex: #{ex}\n"
end

